package com.fivetuat.fsoftprojectserver.httpsClients;

import com.fivetuat.fsoftprojectserver.dtos.https.brevo.EmailRequest;
import com.fivetuat.fsoftprojectserver.dtos.https.brevo.EmailResponse;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.service.annotation.HttpExchange;
import org.springframework.web.service.annotation.PostExchange;

@HttpExchange
public interface BrevoEmailClient {
    @PostExchange(value = "/v3/smtp/email", contentType = "application/json")
    EmailResponse sendEmail(
            @RequestHeader("api-key") String apiKey,
            @RequestBody EmailRequest request
    );
}
