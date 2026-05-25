package com.okanetransfer.service;

import com.okanetransfer.entity.Transfer;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;

@Service
public class ReceiptPrintingService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public String generateTransferReceipt(Transfer transfer, BigDecimal fees) {
        StringBuilder receipt = new StringBuilder();
        
        receipt.append("===============================================\n");
        receipt.append("           OKANE TRANSFER - REÇU\n");
        receipt.append("===============================================\n\n");
        
        receipt.append("Date: ").append(transfer.getCreatedAt().format(DATE_FORMATTER)).append("\n");
        receipt.append("Code de transfert: ").append(transfer.getTransferCode()).append("\n\n");
        
        receipt.append("EXPÉDITEUR:\n");
        receipt.append("Agent: ").append(transfer.getSender().getUsername()).append("\n");
        receipt.append("Agence: ").append(transfer.getAgency().getName()).append("\n");
        receipt.append("Pays: ").append(transfer.getSenderCountry()).append("\n\n");
        
        receipt.append("BÉNÉFICIAIRE:\n");
        receipt.append("Nom: ").append(transfer.getRecipientName()).append("\n");
        receipt.append("Téléphone: ").append(transfer.getRecipientPhone()).append("\n");
        receipt.append("Pays: ").append(transfer.getRecipientCountry()).append("\n\n");
        
        receipt.append("DÉTAILS DU TRANSFERT:\n");
        receipt.append("Montant envoyé: ").append(transfer.getAmount()).append(" ").append(transfer.getCurrency()).append("\n");
        
        if (transfer.getConvertedAmount() != null && !transfer.getAmount().equals(transfer.getConvertedAmount())) {
            receipt.append("Montant converti: ").append(transfer.getConvertedAmount()).append(" ").append(transfer.getTargetCurrency()).append("\n");
        }
        
        receipt.append("Frais: ").append(fees).append(" ").append(transfer.getCurrency()).append("\n");
        receipt.append("Total débité: ").append(transfer.getAmount().add(fees)).append(" ").append(transfer.getCurrency()).append("\n\n");
        
        receipt.append("Statut: ").append(getStatusText(transfer.getStatus())).append("\n\n");
        
        receipt.append("INSTRUCTIONS:\n");
        receipt.append("- Communiquez le code de transfert au bénéficiaire\n");
        receipt.append("- Le bénéficiaire doit présenter une pièce d'identité\n");
        receipt.append("- Conservez ce reçu comme preuve de transaction\n\n");
        
        receipt.append("===============================================\n");
        receipt.append("        Merci de votre confiance\n");
        receipt.append("===============================================\n");
        
        return receipt.toString();
    }

    public String generateWithdrawalReceipt(Transfer transfer) {
        StringBuilder receipt = new StringBuilder();
        
        receipt.append("===============================================\n");
        receipt.append("        OKANE TRANSFER - RETRAIT\n");
        receipt.append("===============================================\n\n");
        
        receipt.append("Date de retrait: ").append(java.time.LocalDateTime.now().format(DATE_FORMATTER)).append("\n");
        receipt.append("Code de transfert: ").append(transfer.getTransferCode()).append("\n\n");
        
        receipt.append("BÉNÉFICIAIRE:\n");
        receipt.append("Nom: ").append(transfer.getRecipientName()).append("\n");
        receipt.append("Téléphone: ").append(transfer.getRecipientPhone()).append("\n\n");
        
        receipt.append("DÉTAILS DU RETRAIT:\n");
        BigDecimal amountReceived = transfer.getConvertedAmount() != null ? 
            transfer.getConvertedAmount() : transfer.getAmount();
        String currency = transfer.getTargetCurrency() != null ? 
            transfer.getTargetCurrency().toString() : transfer.getCurrency().toString();
            
        receipt.append("Montant reçu: ").append(amountReceived).append(" ").append(currency).append("\n");
        receipt.append("Date d'envoi: ").append(transfer.getCreatedAt().format(DATE_FORMATTER)).append("\n\n");
        
        receipt.append("AGENCE DE RETRAIT:\n");
        receipt.append("Nom: ").append(transfer.getAgency().getName()).append("\n");
        receipt.append("Adresse: ").append(transfer.getAgency().getAddress()).append("\n\n");
        
        receipt.append("===============================================\n");
        receipt.append("     Transaction terminée avec succès\n");
        receipt.append("===============================================\n");
        
        return receipt.toString();
    }

    public String generateDailyReport(String agencyName, java.time.LocalDate date, 
                                    int totalTransfers, BigDecimal totalAmount, 
                                    BigDecimal totalFees, BigDecimal cashBalance) {
        StringBuilder report = new StringBuilder();
        
        report.append("===============================================\n");
        report.append("        RAPPORT JOURNALIER - AGENCE\n");
        report.append("===============================================\n\n");
        
        report.append("Agence: ").append(agencyName).append("\n");
        report.append("Date: ").append(date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("\n");
        report.append("Heure d'impression: ").append(java.time.LocalDateTime.now().format(DATE_FORMATTER)).append("\n\n");
        
        report.append("RÉSUMÉ DES TRANSACTIONS:\n");
        report.append("Nombre de transferts: ").append(totalTransfers).append("\n");
        report.append("Montant total transféré: ").append(totalAmount).append("\n");
        report.append("Total des frais collectés: ").append(totalFees).append("\n\n");
        
        report.append("SOLDE DE CAISSE:\n");
        report.append("Solde actuel: ").append(cashBalance).append("\n\n");
        
        report.append("===============================================\n");
        report.append("           Fin du rapport\n");
        report.append("===============================================\n");
        
        return report.toString();
    }

    public void printReceipt(String receiptContent) {
        // Simulation d'impression - dans un vrai environnement, 
        // ceci interfacerait avec une imprimante physique
        System.out.println("=== IMPRESSION REÇU ===");
        System.out.println(receiptContent);
        System.out.println("=== FIN IMPRESSION ===");
        
        // Pour une vraie implémentation, vous pourriez utiliser:
        // - Java Print Service API
        // - Bibliothèques comme iText pour PDF
        // - Intégration avec des imprimantes thermiques via USB/Série
    }

    private String getStatusText(com.okanetransfer.enums.TransferStatus status) {
        return switch (status) {
            case PENDING -> "En attente";
            case VALIDATED -> "Validé";
            case PAID -> "Payé";
            case CANCELLED -> "Annulé";
            case EXPIRED -> "Expiré";
        };
    }
}