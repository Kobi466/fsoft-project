package com.fivetuat.fsoftprojectserver.services;

public interface NotificationEmailService {
    void sendWelcomeEmail(String toEmail, String name);
    void sendForgotPasswordEmail(String toEmail, String name, String otp);
    void sendDeckSharedEmail(String toEmail, String recipientName, String ownerName, String deckTitle, String permission);
    void sendDeckInviteEmail(String toEmail, String ownerName, String deckTitle);
    void sendDailyReminderEmail(String toEmail, String name, int currentStreak, long dueCardsCount);
}
