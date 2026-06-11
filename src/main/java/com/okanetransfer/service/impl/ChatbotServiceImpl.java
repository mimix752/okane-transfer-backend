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
import com.okanetransfer.util.SecurityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.Normalizer;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class ChatbotServiceImpl implements ChatbotService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private ChatEscalationRepository chatEscalationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private GroqChatService groqChatService;

    @Override
    @Transactional
    public ChatMessageResponseDTO chat(ChatMessageRequestDTO dto) {
        User user = getConnectedUser();

        String sessionId = dto.getSessionId().trim();
        String userMessage = dto.getMessage().trim();

        chatMessageRepository.save(new ChatMessage(
                sessionId,
                user,
                ChatRole.USER,
                userMessage
        ));

        if (shouldEscalate(userMessage)) {
            String summary = buildEscalationSummary(user, sessionId, userMessage);

            chatEscalationRepository.save(new ChatEscalation(
                    sessionId,
                    user,
                    summary
            ));

            String reply = "Votre demande a été transmise à un agent. "
                    + "Un conseiller va examiner votre conversation et vous appelez dès que possible.";

            chatMessageRepository.save(new ChatMessage(
                    sessionId,
                    user,
                    ChatRole.BOT,
                    reply
            ));

            return new ChatMessageResponseDTO(reply, true);
        }


        List<ChatMessage> history = getHistoryForGroq(user, sessionId);

        String reply = groqChatService.ask(userMessage, history)
                .orElseGet(() -> {

                    return generateFaqReply(userMessage);
                });

        chatMessageRepository.save(new ChatMessage(
                sessionId,
                user,
                ChatRole.BOT,
                reply
        ));

        return new ChatMessageResponseDTO(reply, false);
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
                        "Utilisateur connecté non trouvé : " + username
                ));
    }

    private boolean shouldEscalate(String message) {
        String normalized = normalize(message);
        return normalized.contains("agent")
                || normalized.contains("humain")
                || normalized.contains("human")
                || normalized.contains("support")
                || normalized.contains("conseiller")
                || normalized.contains("bloque")
                || normalized.contains("blocked")
                || normalized.contains("probleme")
                || normalized.contains("problem")
                || normalized.contains("reclamation")
                || normalized.contains("plainte")
                || normalized.contains("urgent");
    }

    private String generateFaqReply(String message) {
        String normalized = normalize(message);

        if (containsAny(normalized, "suivi", "track", "tracking", "statut", "status")) {
            return "Vous pouvez suivre votre transfert depuis l'espace client avec le code de transfert. "
                    + "Allez dans la rubrique Historique ou Suivi, puis entrez le code du transfert.";
        }
        if (containsAny(normalized, "otp", "code", "verification", "connexion", "login")) {
            return "Lors de la connexion, un code OTP peut être demandé pour sécuriser votre compte. "
                    + "Saisissez le code reçu, puis validez pour terminer l'authentification.";
        }
        if (containsAny(normalized, "frais", "fee", "fees", "commission", "cout")) {
            return "Les frais dépendent du montant, de la devise et du corridor de transfert. "
                    + "Ils sont calculés avant la confirmation du transfert.";
        }
        if (containsAny(normalized, "annuler", "cancel", "rembourser", "refund")) {
            return "L'annulation dépend de l'état du transfert. "
                    + "Si le transfert n'a pas encore été payé, contactez un agent ou consultez le détail du transfert.";
        }
        if (containsAny(normalized, "mot de passe", "password", "changer mot de passe")) {
            return "Vous pouvez modifier votre mot de passe depuis la page Paramètres, section Sécurité. "
                    + "Vous devrez saisir votre mot de passe actuel puis le nouveau mot de passe.";
        }
        if (containsAny(normalized, "notification", "email", "push")) {
            return "Vous pouvez gérer vos préférences de notifications depuis la page Paramètres. "
                    + "Les notifications peuvent vous informer des changements de statut de vos transferts.";
        }

        return "Je peux vous aider avec le suivi de transfert, les frais, l'OTP, les notifications, "
                + "la sécurité du compte ou l'annulation d'un transfert. "
                + "Si votre demande est urgente, écrivez « agent » pour être redirigé vers un conseiller.";
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
}