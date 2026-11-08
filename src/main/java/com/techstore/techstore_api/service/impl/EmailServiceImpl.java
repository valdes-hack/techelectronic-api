package com.techstore.techstore_api.service.impl;

import com.techstore.techstore_api.model.Order;
import com.techstore.techstore_api.service.EmailService;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

import org.springframework.scheduling.annotation.Async;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    /**
     * ENVOI DE LA CONFIRMATION AU CLIENT
     */
    @Async
    @Override
    public void sendOrderConfirmation(Order order) {
        try {
            String to = (order.getUser() != null) ? order.getUser().getEmail() : order.getGuestEmail();
            if (to == null || to.trim().isEmpty()) return;

            String orderRef = (order.getOrderNumber() != null) ? order.getOrderNumber() : order.getId().toString();
            String subject = "Confirmation de votre commande PEOC #" + orderRef;
            
            String clientName = (order.getGuestName() != null) ? order.getGuestName() : "Client";

            String htmlContent = 
                "<div style='font-family: sans-serif; color: #333; max-width: 600px; margin: auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 12px;'>" +
                    "<div style='background: #15803d; color: white; padding: 15px; border-radius: 8px; text-align: center;'>" +
                        "<h2 style='margin:0;'>PEOC - Petit Éleveur de l'Ouest Cameroun</h2>" +
                    "</div>" +
                    "<h3 style='color: #15803d; margin-top: 20px;'>Merci pour votre confiance, " + clientName + " !</h3>" +
                    "<p>Votre commande a été reçue et est en cours de préparation par notre équipe.</p>" +
                    "<p><b>Référence :</b> #" + orderRef + "</p>" +
                    "<p><b>Montant total :</b> " + order.getTotalAmount() + " FCFA</p>" +
                    "<p><b>Code de suivi :</b> <span style='color: #15803d; font-weight: bold; background: #dcfce7; padding: 4px 8px; border-radius: 4px;'>" + order.getTrackingToken() + "</span></p>" +
                    "<br><p style='font-size: 13px; color: #64748b;'>À très bientôt chez PEOC (Petit Éleveur de l'Ouest Cameroun) !</p>" +
                "</div>";

            sendHtmlEmail(to, subject, htmlContent);
        } catch (Exception e) {
            System.err.println("❌ Erreur sendOrderConfirmation: " + e.getMessage());
        }
    }

    /**
     * ENVOI DE L'ALERTE À L'ADMINISTRATEUR
     */
    @Async
    @Override
    public void sendAdminAlert(Order order) {
        try {
            String adminEmail = "valdeshacking01@gmail.com"; 
            
            String orderRef = (order.getOrderNumber() != null) ? order.getOrderNumber() : order.getId().toString();
            String subject = "🚨 PEOC - NOUVELLE COMMANDE [#" + orderRef + "]";

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
                mapsSection = "<div style='margin: 20px 0; padding: 15px; background: #f0fdf4; border-radius: 10px; border: 1px solid #bbf7d0;'>" +
                              "📍 <b>LOCALISATION GPS :</b><br>" +
                              "<a href='" + url + "' style='color: #15803d; font-weight: bold;'>Ouvrir l'itinéraire dans Google Maps</a>" +
                              "</div>";
            }

            // 3. Infos Client
            String clientName = (order.getUser() != null) ? 
                (order.getUser().getFirstName() + " " + order.getUser().getLastName()) : order.getGuestName();

            String htmlContent = 
                "<div style='font-family: sans-serif; max-width: 600px; margin: auto; border: 1px solid #e2e8f0; border-radius: 12px; padding: 20px;'>" +
                    "<div style='background: #15803d; color: white; padding: 12px; border-radius: 8px; text-align: center;'>" +
                        "<h2 style='margin: 0;'>PEOC Admin - Alerte Commande</h2>" +
                    "</div>" +
                    "<p style='margin-top: 15px;'><b>👤 Client :</b> " + clientName + "</p>" +
                    "<p><b>🚚 Mode de livraison :</b> " + order.getShippingType() + "</p>" +
                    "<p><b>💰 Mode de paiement :</b> " + order.getPaymentMethod() + "</p>" +
                    mapsSection +
                    "<h3>📦 Articles commandés :</h3>" +
                    itemsTable.toString() +
                    "<h3 style='color: #15803d; text-align: right; margin-top: 20px;'>TOTAL : " + order.getTotalAmount() + " FCFA</h3>" +
                "</div>";

            sendHtmlEmail(adminEmail, subject, htmlContent);
        } catch (Exception e) {
            System.err.println("❌ Erreur sendAdminAlert: " + e.getMessage());
        }
    }

    /**
     * MÉTHODE PRIVÉE : ENVOI RÉEL DU MAIL (Format HTML)
     */
    private void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom("valdeshacking01@gmail.com", "PEOC Elevage");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
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