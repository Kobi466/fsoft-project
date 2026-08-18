package com.fivetuat.fsoftprojectserver.services.implement;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import com.fivetuat.fsoftprojectserver.dtos.https.brevo.EmailRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.brevo.EmailResponse;
import com.fivetuat.fsoftprojectserver.dtos.https.brevo.Recipient;
import com.fivetuat.fsoftprojectserver.dtos.https.brevo.Sender;
import com.fivetuat.fsoftprojectserver.httpsClients.BrevoEmailClient;
import com.fivetuat.fsoftprojectserver.services.NotificationService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationServiceImpl implements NotificationService {

    final BrevoEmailClient brevoEmailClient;
    final TemplateEngine templateEngine;

    @Value("${notification.email.apikey}")
    String apiKey;

    @Value("${notification.email.sender.name}")
    String senderName;

    @Value("${notification.email.sender.email}")
    String senderEmail;

    @Async
    @Retryable(
        retryFor = Exception.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 5000)
    )
    @Override
    public void sendWelcomeEmail(String toEmail, String name) {
        log.info("Sending welcome email to {} (Running asynchronously)", toEmail);

        Sender sender = Sender.builder()
                .name(senderName)
                .email(senderEmail)
                .build();

        Recipient recipient = Recipient.builder()
                .name(name)
                .email(toEmail)
                .build();

        // Render HTML content using Thymeleaf
        Context context = new Context();
        context.setVariable("name", name);
        String htmlContent = templateEngine.process("welcome-email", context);

        EmailRequest emailRequest = EmailRequest.builder()
                .sender(sender)
                .to(List.of(recipient))
                .subject("Chào mừng bạn gia nhập FSoft Flashcards!")
                .htmlContent(htmlContent)
                .build();

        EmailResponse response = brevoEmailClient.sendEmail(apiKey, emailRequest);
        log.info("Welcome email sent successfully to {}. Brevo Message ID: {}", toEmail, response.getMessageId());
    }

    @Recover
    public void recoverWelcomeEmail(Exception e, String toEmail, String name) {
        log.error("CRITICAL: Failed to send welcome email to {} after 3 attempts. Error: {}", toEmail, e.getMessage());
    }

    @Async
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000)
    )
    @Override
    public void sendForgotPasswordEmail(String toEmail, String name, String otp) {
        log.info("Sending forgot password OTP email to {} (Running asynchronously)", toEmail);

        Sender sender = Sender.builder()
                .name(senderName)
                .email(senderEmail)
                .build();

        Recipient recipient = Recipient.builder()
                .name(name)
                .email(toEmail)
                .build();

        Context context = new Context();
        context.setVariable("name", name);
        context.setVariable("otp", otp);
        String htmlContent = templateEngine.process("forgot-password-email", context);

        EmailRequest emailRequest = EmailRequest.builder()
                .sender(sender)
                .to(List.of(recipient))
                .subject("[FSoft Flashcards] Mã xác thực OTP đặt lại mật khẩu")
                .htmlContent(htmlContent)
                .build();

        EmailResponse response = brevoEmailClient.sendEmail(apiKey, emailRequest);
        log.info("OTP email sent successfully to {}. Brevo Message ID: {}", toEmail, response.getMessageId());
    }

    @Recover
    public void recoverForgotPasswordEmail(Exception e, String toEmail, String name, String otp) {
        log.error("CRITICAL: Failed to send forgot password OTP to {} after 3 attempts. Error: {}", toEmail, e.getMessage());
    }

    @Async
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000)
    )
    @Override
    public void sendDeckSharedEmail(String toEmail, String recipientName, String ownerName, String deckTitle, String permission) {
        log.info("Sending deck shared email to {} (Running asynchronously)", toEmail);

        Sender sender = Sender.builder()
                .name(senderName)
                .email(senderEmail)
                .build();

        Recipient recipient = Recipient.builder()
                .name(recipientName != null ? recipientName : toEmail)
                .email(toEmail)
                .build();

        Context context = new Context();
        context.setVariable("recipientName", recipientName != null ? recipientName : toEmail);
        context.setVariable("ownerName", ownerName);
        context.setVariable("deckTitle", deckTitle);
        context.setVariable("permission", permission);
        String htmlContent = templateEngine.process("deck-shared-email", context);

        EmailRequest emailRequest = EmailRequest.builder()
                .sender(sender)
                .to(List.of(recipient))
                .subject("[FSoft Flashcards] Bạn được chia sẻ bộ từ vựng \"" + deckTitle + "\"")
                .htmlContent(htmlContent)
                .build();

        EmailResponse response = brevoEmailClient.sendEmail(apiKey, emailRequest);
        log.info("Deck shared email sent successfully to {}. Brevo Message ID: {}", toEmail, response.getMessageId());
    }

    @Recover
    public void recoverDeckSharedEmail(Exception e, String toEmail, String recipientName, String ownerName, String deckTitle, String permission) {
        log.error("CRITICAL: Failed to send deck shared email to {} after 3 attempts. Error: {}", toEmail, e.getMessage());
    }

    @Async
    @Retryable(
            retryFor = Exception.class,
            maxAttempts = 3,
            backoff = @Backoff(delay = 5000)
    )
    @Override
    public void sendDeckInviteEmail(String toEmail, String ownerName, String deckTitle) {
        log.info("Sending deck invite email to {} (Running asynchronously)", toEmail);

        Sender sender = Sender.builder()
                .name(senderName)
                .email(senderEmail)
                .build();

        Recipient recipient = Recipient.builder()
                .name(toEmail)
                .email(toEmail)
                .build();

        Context context = new Context();
        context.setVariable("ownerName", ownerName);
        context.setVariable("deckTitle", deckTitle);
        String htmlContent = templateEngine.process("deck-invite-email", context);

        EmailRequest emailRequest = EmailRequest.builder()
                .sender(sender)
                .to(List.of(recipient))
                .subject("[FSoft Flashcards] Lời mời học bộ từ vựng \"" + deckTitle + "\"")
                .htmlContent(htmlContent)
                .build();

        EmailResponse response = brevoEmailClient.sendEmail(apiKey, emailRequest);
        log.info("Deck invite email sent successfully to {}. Brevo Message ID: {}", toEmail, response.getMessageId());
    }

    @Recover
    public void recoverDeckInviteEmail(Exception e, String toEmail, String ownerName, String deckTitle) {
        log.error("CRITICAL: Failed to send deck invite email to {} after 3 attempts. Error: {}", toEmail, e.getMessage());
    }
}
