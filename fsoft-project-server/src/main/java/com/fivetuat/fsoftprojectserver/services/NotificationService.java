package com.fivetuat.fsoftprojectserver.services;

public interface NotificationService {
    void sendWelcomeEmail(String toEmail, String name);
    void sendForgotPasswordEmail(String toEmail, String name, String otp);
}
