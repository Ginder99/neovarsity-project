package com.vms.notification.service;

import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import java.io.IOException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    private final SendGrid sendGrid;
    private final EmailTemplateLoader emailTemplateLoader;
    private final String fromEmail;

    public NotificationService(
            SendGrid sendGrid,
            EmailTemplateLoader emailTemplateLoader,
            @Value("${sendgrid.from-email}") String fromEmail) {
        this.sendGrid = sendGrid;
        this.emailTemplateLoader = emailTemplateLoader;
        this.fromEmail = fromEmail;
    }

    public void sendEmailNotification(String notificationType, String recipientEmail) {
        EmailTemplate template = emailTemplateLoader.load(notificationType);
        Mail mail = new Mail(
                new Email(fromEmail),
                template.subject(),
                new Email(recipientEmail),
                new Content("text/plain", template.content()));

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");

        try {
            request.setBody(mail.build());
            Response response = sendGrid.api(request);
            if (response.getStatusCode() >= 400) {
                throw new IllegalStateException("Failed to send notification email");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to send notification email", exception);
        }
    }
}
