package com.example.bloodmatch.observer;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.example.bloodmatch.model.Donor;

import java.io.IOException;

/**
 * Concrete observer for sending email notifications via SendGrid.
 */
@Component
public class EmailNotificationObserver implements NotificationObserver {
    private static final Logger logger = LoggerFactory.getLogger(EmailNotificationObserver.class);

    @Value("${sendgrid.api-key}")
    private String sendGridApiKey;

    @Value("${sendgrid.from-email}")
    private String fromEmailAddress;

    @Override
    public void update(Donor donor, String subject, String messageText) {
        if (donor.getEmail() == null || donor.getEmail().isEmpty()) {
            logger.warn("No email address found for donor: {}", donor.getName());
            return;
        }

        // If the message already contains HTML tags (like <br> or <strong>), don't replace \n
        String formattedMessage = messageText.contains("<") ? messageText : messageText.replace("\n", "<br>");
        
        boolean isEmergency = subject.contains("URGENT") || subject.contains("EMERGENCY");
        String headerColor = isEmergency ? "#c0392b" : "#2980b9";
        String headerIcon = isEmergency ? "&#x1F6A8;" : "&#128167;";
        String title = isEmergency ? "Emergency Blood Requirement" : "You're Eligible to Donate Blood!";

        String htmlBody = String.format(
            "<html><body style='font-family:Arial,sans-serif;color:#333;line-height:1.6;'>" +
            "<div style='max-width:600px;margin:auto;padding:30px;border:1px solid #e0e0e0;border-radius:12px;background-color:#ffffff;box-shadow: 0 4px 6px rgba(0,0,0,0.05);'>" +
            "<div style='text-align:center;margin-bottom:20px;'>" +
            "<h2 style='color:%s;margin:0;'>%s %s</h2>" +
            "</div>" +
            "<div style='margin-bottom:25px;background-color:#fafafa;padding:20px;border-radius:8px;border-left:4px solid %s;'>%s</div>" +
            "<p style='color:#888;font-size:12px;border-top:1px solid #eee;padding-top:15px;text-align:center;'>" +
            "This is an automated message from <strong>BloodMatch Team</strong>.<br>" +
            "Thank you for being a life saver!</p>" +
            "</div></body></html>", 
            headerColor, headerIcon, title, headerColor, formattedMessage);

        Email from = new Email(fromEmailAddress, "BloodMatch Team");
        Email to = new Email(donor.getEmail());
        Content content = new Content("text/html", htmlBody);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(sendGridApiKey);
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
            if (response.getStatusCode() >= 200 && response.getStatusCode() < 300) {
                logger.info("Eligibility notification sent to {} (Status: {})", donor.getEmail(), response.getStatusCode());
            } else {
                logger.error("SendGrid rejected email to {} (Status: {}) Body: {}", donor.getEmail(), response.getStatusCode(), response.getBody());
            }
        } catch (IOException ex) {
            logger.error("Error sending SendGrid email to {}: {}", donor.getEmail(), ex.getMessage());
        }
    }
}
