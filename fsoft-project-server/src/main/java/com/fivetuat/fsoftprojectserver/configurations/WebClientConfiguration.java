package com.fivetuat.fsoftprojectserver.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;

import com.fivetuat.fsoftprojectserver.httpsClients.BrevoEmailClient;

@Configuration
public class WebClientConfiguration {
    @Value("${notification.email.url}")
    private String brevoUrl;

//   @Bean
//   VnPayClient vnPayClient(WebClient.Builder builder) {
//      WebClient webClient = builder.build();
//      HttpServiceProxyFactory factory = HttpServiceProxyFactory.builderFor(WebClientAdapter.create(webClient)).build();
//      return factory.createClient(VnPayClient.class);
//    }
//
    @Bean
    public BrevoEmailClient brevoEmailClient() {
        WebClient webClient = WebClient.builder().baseUrl(brevoUrl).build();
        HttpServiceProxyFactory factory = HttpServiceProxyFactory
                .builderFor(WebClientAdapter.create(webClient))
                .build();
        return factory.createClient(BrevoEmailClient.class);
    }
}
