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

        String htmlBody = String.format(
            "<html><body style='font-family:Arial,sans-serif;color:#333;'>" +
            "<div style='max-width:600px;margin:auto;padding:30px;border:1px solid #e0e0e0;border-radius:8px;'>" +
            "<h2 style='color:#c0392b;'>&#128167; You're Eligible to Donate Blood!</h2>" +
            "<p>Hello <strong>%s</strong>,</p>" +
            "<p>It has been more than <strong>90 days</strong> since your last donation (or you are a new potential donor). " +
            "You are now eligible to donate blood again! Your contribution can make a huge difference.</p>" +
            "<p>Please visit your nearest blood bank or check the <strong>BloodMatch</strong> app for active requests.</p>" +
            "<br><p style='color:#888;font-size:12px;'>Thank you,<br><strong>BloodMatch Team</strong></p>" +
            "</div></body></html>", donor.getName());

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
