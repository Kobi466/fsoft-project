package com.fivetuat.fsoftprojectserver.services.implement;

import com.fivetuat.fsoftprojectserver.dtos.https.brevo.EmailRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.brevo.Recipient;
import com.fivetuat.fsoftprojectserver.dtos.https.brevo.Sender;
import com.fivetuat.fsoftprojectserver.httpsClients.BrevoEmailClient;
import com.fivetuat.fsoftprojectserver.services.NotificationEmailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationEmailServiceImpl implements NotificationEmailService {

    BrevoEmailClient brevoEmailClient;

    @NonFinal
    @Value("${notification.email.apikey}")
    String apiKey;

    @NonFinal
    @Value("${notification.email.sender.name}")
    String senderName;

    @NonFinal
    @Value("${notification.email.sender.email}")
    String senderEmail;

    @Override
    public void sendWelcomeEmail(String toEmail, String name) {
        String subject = "Chào mừng bạn đến với FSoft Flashcards!";
        String content = "<h3>Xin chào " + name + ",</h3><p>Cảm ơn bạn đã đăng ký tài khoản tại FSoft Flashcards. Chúc bạn có trải nghiệm học tập hiệu quả!</p>";
        sendEmail(toEmail, name, subject, content);
    }

    @Override
    public void sendForgotPasswordEmail(String toEmail, String name, String otp) {
        String subject = "Mã OTP khôi phục mật khẩu";
        String content = "<h3>Xin chào " + name + ",</h3><p>Mã OTP để đặt lại mật khẩu của bạn là: <b>" + otp + "</b>. Mã này có hiệu lực trong 10 phút.</p>";
        sendEmail(toEmail, name, subject, content);
    }

    @Override
    public void sendDeckSharedEmail(String toEmail, String recipientName, String ownerName, String deckTitle, String permission) {
        String subject = "Bạn vừa được chia sẻ một bộ bài học mới!";
        String content = "<h3>Xin chào " + recipientName + ",</h3><p><b>" + ownerName + "</b> đã chia sẻ bộ bài <b>" + deckTitle + "</b> với quyền: <i>" + permission + "</i>.</p>";
        sendEmail(toEmail, recipientName, subject, content);
    }

    @Override
    public void sendDeckInviteEmail(String toEmail, String ownerName, String deckTitle) {
        String subject = "Lời mời tham gia học trên FSoft Flashcards";
        String content = "<h3>Xin chào,</h3><p><b>" + ownerName + "</b> muốn chia sẻ bộ bài <b>" + deckTitle + "</b> với bạn trên FSoft Flashcards. Hãy đăng ký tài khoản để bắt đầu học ngay!</p>";
        sendEmail(toEmail, toEmail, subject, content);
    }

    @Override
    public void sendDailyReminderEmail(String toEmail, String name, int streakCount) {
        String subject = "Nhắc nhở duy trì chuỗi ngày học!";
        String content = "<h3>Xin chào " + name + ",</h3><p>Bạn đã duy trì chuỗi học <b>" + streakCount + " ngày</b>! Đừng quên học hôm nay để không bị đứt chuỗi nhé!</p>";
        sendEmail(toEmail, name, subject, content);
    }

    private void sendEmail(String toEmail, String recipientName, String subject, String htmlContent) {
        try {
            EmailRequest request = EmailRequest.builder()
                    .sender(new Sender(senderName, senderEmail))
                    .to(List.of(new Recipient(toEmail, recipientName)))
                    .subject(subject)
                    .htmlContent(htmlContent)
                    .build();

            brevoEmailClient.sendEmail(apiKey, request);
            log.info("Email sent successfully to {}", toEmail);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", toEmail, e.getMessage(), e);
        }
    }
}
