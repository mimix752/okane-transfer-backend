package com.okanetransfer.service.impl;

import com.okanetransfer.dto.request.ChatMessageRequestDTO;
import com.okanetransfer.dto.response.ChatMessageResponseDTO;
import com.okanetransfer.entity.ChatEscalation;
import com.okanetransfer.entity.ChatMessage;
import com.okanetransfer.entity.User;
import com.okanetransfer.enums.ChatRole;
import com.okanetransfer.exception.ResourceNotFoundException;
import com.okanetransfer.repository.ChatEscalationRepository;
import com.okanetransfer.repository.ChatMessageRepository;
import com.okanetransfer.repository.UserRepository;
import com.okanetransfer.service.ChatbotService;
import com.okanetransfer.service.GroqChatService;
import com.okanetransfer.service.SemanticFaqService;
import com.okanetransfer.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.okanetransfer.service.LanguageDetectorService;
import java.text.Normalizer;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    private static final List<String> HARD_ESCALATION = List.of(
            "agent", "humain", "human", "conseiller"
    );

    private static final List<String> SOFT_ESCALATION = List.of(
            "bloque", "blocked", "probleme", "problem",
            "reclamation", "plainte", "urgent", "fraude",
            "disparu", "vol", "arnaque"
    );

    @Value("${chatbot.rate.limit.hourly:30}")
    private int hourlyLimit;

    @Value("${chatbot.max.message.length:500}")
    private int maxMessageLength;

    @Value("${chatbot.soft.escalation.min.messages:3}")
    private int minMessagesForSoftEscalation;

    @Autowired private ChatMessageRepository chatMessageRepository;
    @Autowired private ChatEscalationRepository chatEscalationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private GroqChatService groqChatService;
    @Autowired private SemanticFaqService semanticFaqService;
    @Autowired private LanguageDetectorService languageDetectorService;

    @Override
    @Transactional
    public ChatMessageResponseDTO chat(ChatMessageRequestDTO dto) {
        User user = getConnectedUser();
        String sessionId = dto.getSessionId().trim();
        String userMessage = dto.getMessage().trim();
        String detectedLang = languageDetectorService.detect(userMessage);


        // ── Gate 1: input sanity ──────────────────────────────────────────
        if (userMessage.isBlank()) {
            return noSave(i18n("empty_message", detectedLang));        }

        if (userMessage.length() > maxMessageLength) {
            return noSave(i18n("too_long", detectedLang));        }

        // ── Gate 2: rate limiting ─────────────────────────────────────────
        LocalDateTime oneHourAgo = LocalDateTime.now().minusHours(1);
        long recentCount = chatMessageRepository
                .countByUserAndRoleAndCreatedAtAfter(user, ChatRole.USER, oneHourAgo);

        if (recentCount >= hourlyLimit) {
            return noSave(i18n("rate_limit", detectedLang));
        }

        // ── Save user message (after gates, before processing) ────────────
        chatMessageRepository.save(new ChatMessage(sessionId, user, ChatRole.USER, userMessage));

        // ── Gate 3: repetition guard ──────────────────────────────────────
        if (isRepetitive(user, sessionId, userMessage)) {
            if (isRepetitive(user, sessionId, userMessage)) {
                return saveAndReturn(sessionId, user, i18n("repetitive", detectedLang), false);
            }
        }

        // ── Gate 4: escalation check ──────────────────────────────────────
        String normalized = normalize(userMessage);
        long sessionMessageCount = chatMessageRepository
                .countByUserAndSessionId(user, sessionId);

        if (isHardEscalation(normalized)
                || (isSoftEscalation(normalized) && sessionMessageCount >= minMessagesForSoftEscalation)) {
            return handleEscalation(sessionId, user, userMessage,detectedLang);
        }

        // ── Gate 5: semantic FAQ ──────────────────────────────────────────
        var faqResult = semanticFaqService.findAnswer(userMessage);
        if (faqResult.isPresent()) {
            SemanticFaqServiceImpl.FaqResult faq = faqResult.get();
            if (faq.escalation()) return handleEscalation(sessionId, user, userMessage, detectedLang);

            String answer = faq.answer();
            if (!"fr".equals(detectedLang)) {
                answer = groqChatService.translate(answer, detectedLang); // ADD THIS
            }
            return saveAndReturn(sessionId, user, answer, false);
        }

        // ── Gate 6: Groq ──────────────────────────────────────────────────
        List<ChatMessage> history = getHistoryForGroq(user, sessionId);
        String reply = groqChatService.ask(userMessage, history,detectedLang)
                .orElseGet(() -> generateFaqReply(userMessage,detectedLang));

        return saveAndReturn(sessionId, user, reply, false);
    }

    // ─────────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────────

    private ChatMessageResponseDTO handleEscalation(String sessionId, User user, String userMessage, String detectedLang) {
        String summary = buildEscalationSummary(user, sessionId, userMessage);
        chatEscalationRepository.save(new ChatEscalation(sessionId, user, summary));
        return saveAndReturn(sessionId, user, i18n("escalation", detectedLang), true);
    }

    private ChatMessageResponseDTO saveAndReturn(String sessionId, User user,
                                                 String reply, boolean escalated) {
        chatMessageRepository.save(new ChatMessage(sessionId, user, ChatRole.BOT, reply));
        return new ChatMessageResponseDTO(reply, escalated);
    }

    /** Returns a response without saving anything to DB — used for gate rejections. */
    private ChatMessageResponseDTO noSave(String message) {
        return new ChatMessageResponseDTO(message, false);
    }

    private boolean isRepetitive(User user, String sessionId, String userMessage) {
        List<ChatMessage> lastBotMessages = chatMessageRepository
                .findTop2ByUserAndSessionIdAndRoleOrderByCreatedAtDesc(
                        user, sessionId, ChatRole.USER);

        if (lastBotMessages.size() < 2) return false;

        String normalizedCurrent = normalize(userMessage);
        long identicalCount = lastBotMessages.stream()
                .map(m -> normalize(m.getContent()))
                .filter(c -> c.equals(normalizedCurrent))
                .count();

        return identicalCount >= 2;
    }

    private boolean isHardEscalation(String normalized) {
        return HARD_ESCALATION.stream().anyMatch(normalized::contains);
    }

    private boolean isSoftEscalation(String normalized) {
        return SOFT_ESCALATION.stream().anyMatch(normalized::contains);
    }

    private List<ChatMessage> getHistoryForGroq(User user, String sessionId) {
        return chatMessageRepository
                .findTop10ByUserAndSessionIdOrderByCreatedAtDesc(user, sessionId)
                .stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .collect(Collectors.toList());
    }

    private User getConnectedUser() {
        String username = SecurityUtils.getCurrentUsername();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Utilisateur connecté non trouvé : " + username));
    }

    private String buildEscalationSummary(User user, String sessionId, String latestMessage) {
        List<ChatMessage> lastMessages = chatMessageRepository
                .findTop5ByUserAndSessionIdOrderByCreatedAtDesc(user, sessionId);

        String conversation = lastMessages.stream()
                .sorted(Comparator.comparing(ChatMessage::getCreatedAt))
                .map(m -> m.getRole() + ": " + m.getContent())
                .collect(Collectors.joining("\n"));

        return "Client: " + user.getUsername()
                + "\nSession: " + sessionId
                + "\nDernier message: " + latestMessage
                + "\n\nDerniers échanges:\n" + conversation;
    }

    private String generateFaqReply(String message, String detectedLang) {
        String normalized = normalize(message);
        String frenchAnswer = null;

        if (containsAny(normalized,
                "suivi", "track", "statut", "status",
                "تتبع", "حالة", "وضع", "تحويل"))
            frenchAnswer = "Vous pouvez suivre votre transfert depuis l'espace client avec le code de transfert. "
                    + "Allez dans la rubrique Historique ou Suivi, puis entrez le code du transfert.";

        else if (containsAny(normalized,
                "otp", "code", "verification", "connexion", "login",
                "رمز", "تحقق", "دخول", "كلمة السر"))
            frenchAnswer = "Lors de la connexion, un code OTP peut être demandé pour sécuriser votre compte. "
                    + "Saisissez le code reçu, puis validez pour terminer l'authentification.";

        else if (containsAny(normalized,
                "frais", "fee", "commission", "cout",
                "رسوم", "عمولة", "تكلفة", "سعر"))
            frenchAnswer = "Les frais dépendent du montant, de la devise et du corridor de transfert. "
                    + "Ils sont calculés avant la confirmation du transfert.";

        else if (containsAny(normalized,
                "annuler", "cancel", "rembourser", "refund",
                "الغاء", "إلغاء", "استرداد", "ارجاع"))
            frenchAnswer = "L'annulation dépend de l'état du transfert. "
                    + "Si le transfert n'a pas encore été payé, consultez le détail du transfert.";

        else if (containsAny(normalized,
                "mot de passe", "password",
                "كلمة مرور", "باسورد", "نسيت"))
            frenchAnswer = "Vous pouvez modifier votre mot de passe depuis Paramètres > Sécurité.";

        else if (containsAny(normalized,
                "notification", "email", "push",
                "اشعار", "إشعار", "بريد"))
            frenchAnswer = "Gérez vos notifications depuis Paramètres > Notifications.";

        if (frenchAnswer == null) {
            return i18n("faq_fallback", detectedLang);
        }

        // already French, skip the API call
        if ("fr".equals(detectedLang)) return frenchAnswer;

        return groqChatService.translate(frenchAnswer, detectedLang);
    }
    private boolean containsAny(String value, String... keywords) {
        for (String keyword : keywords) {
            if (value.contains(normalize(keyword))) return true;
        }
        return false;
    }

    private String normalize(String value) {
        if (value == null) return "";
        String lower = value.toLowerCase(Locale.ROOT);
        return Normalizer.normalize(lower, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }

    private static final Map<String, Map<String, String>> I18N = Map.of(
            "empty_message", Map.of(
                    "fr", "Votre message est vide. Comment puis-je vous aider ?",
                    "ar", "رسالتك فارغة. كيف يمكنني مساعدتك؟",
                    "en", "Your message is empty. How can I help you?"
            ),
            "too_long", Map.of(
                    "fr", "Votre message est trop long. Merci de résumer votre question en quelques mots.",
                    "ar", "رسالتك طويلة جداً. يرجى تلخيص سؤالك في بضع كلمات.",
                    "en", "Your message is too long. Please summarize your question in a few words."
            ),
            "rate_limit", Map.of(
                    "fr", "Vous avez atteint la limite de messages par heure. Veuillez réessayer plus tard ou écrire « agent » pour contacter un conseiller.",
                    "ar", "لقد وصلت إلى الحد الأقصى للرسائل في الساعة. يرجى المحاولة لاحقاً أو اكتب «agent» للتواصل مع مستشار.",
                    "en", "You have reached the hourly message limit. Please try again later or type 'agent' to contact an advisor."
            ),
            "repetitive", Map.of(
                    "fr", "Je vous ai déjà répondu à cette question. Écrivez « agent » si vous avez besoin d'une aide supplémentaire.",
                    "ar", "لقد أجبتك على هذا السؤال من قبل. اكتب «agent» إذا كنت بحاجة إلى مزيد من المساعدة.",
                    "en", "I already answered this question. Type 'agent' if you need further help."
            ),
            "escalation", Map.of(
                    "fr", "Votre demande a été transmise à un conseiller. Un agent va examiner votre conversation et vous contacter dès que possible.",
                    "ar", "تم تحويل طلبك إلى مستشار. سيقوم أحد الوكلاء بمراجعة محادثتك والتواصل معك في أقرب وقت.",
                    "en", "Your request has been forwarded to an advisor. An agent will review your conversation and contact you shortly."
            ),
            "faq_fallback", Map.of(
                    "fr", "Je peux vous aider avec le suivi, les frais, l'OTP, les notifications ou l'annulation. Écrivez « agent » pour être redirigé vers un conseiller.",
                    "ar", "يمكنني مساعدتك في تتبع التحويل، الرسوم، رمز OTP، الإشعارات أو الإلغاء. اكتب «agent» للتحدث مع مستشار.",
                    "en", "I can help with transfer tracking, fees, OTP, notifications or cancellation. Type 'agent' to be redirected to an advisor."
            )
    );

    private String i18n(String key, String lang) {
        return I18N.getOrDefault(key, Map.of())
                .getOrDefault(lang,
                        I18N.get(key).getOrDefault("fr", "?"));
    }
}