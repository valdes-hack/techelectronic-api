package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.model.Order;
import com.techstore.techstore_api.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    /**
     * ENVOI DE LA CONFIRMATION AU CLIENT
     */
    @Override
    public void sendOrderConfirmation(Order order) {
        String to = (order.getUser() != null) ? order.getUser().getEmail() : order.getGuestEmail();
        if (to == null) return;

        String orderRef = (order.getOrderNumber() != null) ? order.getOrderNumber() : order.getId().toString();
        String subject = "Confirmation de votre commande TechStore #" + orderRef;
        
        String clientName = (order.getGuestName() != null) ? order.getGuestName() : "Client";

        String htmlContent = 
            "<div style='font-family: sans-serif; color: #333;'>" +
                "<h2>Merci pour votre confiance, " + clientName + " !</h2>" +
                "<p>Votre commande est bien enregistrée et en cours de préparation.</p>" +
                "<p><b>Référence :</b> " + orderRef + "</p>" +
                "<p><b>Montant total :</b> " + order.getTotalAmount() + " FCFA</p>" +
                "<p><b>Code de suivi :</b> <span style='color: #0066CC; font-weight: bold;'>" + order.getTrackingToken() + "</span></p>" +
                "<br><p>À bientôt sur TechStore !</p>" +
            "</div>";

        sendHtmlEmail(to, subject, htmlContent);
    }

    /**
     * ENVOI DE L'ALERTE À L'ADMINISTRATEUR
     */
    @Override
    public void sendAdminAlert(Order order) {
        // CHANGE CET EMAIL PAR LE TIEN ✨
        String adminEmail = "valdeshacking01@gmail.com"; 
        
        String orderRef = (order.getOrderNumber() != null) ? order.getOrderNumber() : order.getId().toString();
        String subject = "🚨 NOUVELLE COMMANDE [" + orderRef + "]";

        // 1. Construction du tableau des articles
        StringBuilder itemsTable = new StringBuilder();
        itemsHtmlHeader(itemsTable);
        
        if (order.getItems() != null) {
            for (var item : order.getItems()) {
                BigDecimal subTotal = item.getUnitPrice().multiply(new BigDecimal(item.getQuantity()));
                itemsTable.append("<tr style='text-align: center;'>")
                        .append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(item.getProductName()).append("</td>")
                        .append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(item.getQuantity()).append("</td>")
                        .append("<td style='padding: 10px; border: 1px solid #ddd;'>").append(item.getUnitPrice()).append(" F</td>")
                        .append("<td style='padding: 10px; border: 1px solid #ddd; font-weight: bold;'>").append(subTotal).append(" F</td>")
                        .append("</tr>");
            }
        }
        itemsTable.append("</table>");

        // 2. Lien Google Maps
        String mapsSection = "";
        if (order.getLatitude() != null && order.getLongitude() != null) {
            String url = "https://www.google.com/maps?q=" + order.getLatitude() + "," + order.getLongitude();
            mapsSection = "<div style='margin: 20px 0; padding: 15px; background: #e7f3ff; border-radius: 10px;'>" +
                          "📍 <b>LOCALISATION GPS :</b><br>" +
                          "<a href='" + url + "' style='color: #0066CC; font-weight: bold;'>Ouvrir l'itinéraire dans Google Maps</a>" +
                          "</div>";
        }

        // 3. Infos Client
        String clientName = (order.getUser() != null) ? 
            (order.getUser().getFirstName() + " " + order.getUser().getLastName()) : order.getGuestName();

        String htmlContent = 
            "<div style='font-family: sans-serif; max-width: 600px; margin: auto; border: 1px solid #eee; padding: 20px;'>" +
                "<h2 style='color: #0066CC;'>Nouvelle Commande Reçue</h2>" +
                "<p><b>👤 Client :</b> " + clientName + "</p>" +
                "<p><b>🚚 Mode :</b> " + order.getShippingType() + "</p>" +
                "<p><b>💰 Paiement :</b> " + order.getPaymentMethod() + "</p>" +
                mapsSection +
                "<h3>📦 Articles :</h3>" +
                itemsTable.toString() +
                "<h3 style='color: #d32f2f; text-align: right;'>TOTAL : " + order.getTotalAmount() + " FCFA</h3>" +
            "</div>";

        sendHtmlEmail(adminEmail, subject, htmlContent);
    }

    /**
     * MÉTHODE PRIVÉE : ENVOI RÉEL DU MAIL (Format HTML)
     * C'est cette méthode qui enlève le rouge ! ✨
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true); // true = autorise le HTML
            
            mailSender.send(message);
            System.out.println("✅ Email envoyé avec succès à : " + to);
        } catch (Exception e) {
            System.err.println("❌ ERREUR ENVOI MAIL : " + e.getMessage());
        }
    }

    /**
     * MÉTHODE PRIVÉE : EN-TÊTE DU TABLEAU
     */
    private void itemsHtmlHeader(StringBuilder sb) {
        sb.append("<table style='width: 100%; border-collapse: collapse; margin-top: 10px;'>")
          .append("<tr style='background: #0066CC; color: white;'>")
          .append("<th style='padding: 10px; border: 1px solid #ddd;'>Produit</th>")
          .append("<th style='padding: 10px; border: 1px solid #ddd;'>Qté</th>")
          .append("<th style='padding: 10px; border: 1px solid #ddd;'>Prix</th>")
          .append("<th style='padding: 10px; border: 1px solid #ddd;'>Total</th>")
          .append("</tr>");
    }
}