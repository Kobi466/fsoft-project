package com.fivetuat.fsoftprojectserver.configurations;

import com.fivetuat.fsoftprojectserver.httpsClients.BrevoEmailClient;
import com.fivetuat.fsoftprojectserver.httpsClients.GoogleOauth2Client;
import com.fivetuat.fsoftprojectserver.httpsClients.GoogleUserInfoClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.support.WebClientAdapter;
import org.springframework.web.service.invoker.HttpServiceProxyFactory;


@Configuration
public class WebClientConfiguration {

	@Value("${notification.email.url}")
	private String brevoUrl;

	@Bean
	public WebClient.Builder webClient(){
		return WebClient.builder();
	}

	@Bean
	public GoogleOauth2Client googleOauth2Client(WebClient.Builder webClientBuilder){
		WebClient client = webClientBuilder
//				.baseUrl("https://oauth2.googleapis.com")
				.build();
		WebClientAdapter webClientAdapter =WebClientAdapter.create(client);
		HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
				.builderFor(webClientAdapter)
				.build();
		return httpServiceProxyFactory.createClient(GoogleOauth2Client.class);
	}

	@Bean
	public GoogleUserInfoClient googleUserInfoClient(WebClient.Builder builder){
		WebClient client = builder.build();
		WebClientAdapter webClientAdapter = WebClientAdapter.create(client);
		HttpServiceProxyFactory httpServiceProxyFactory = HttpServiceProxyFactory
				.builderFor(webClientAdapter)
				.build();
		return httpServiceProxyFactory.createClient(GoogleUserInfoClient.class);
	}
	@Bean
	public BrevoEmailClient brevoEmailClient() {
		WebClient webClient = WebClient.builder().baseUrl(brevoUrl).build();
		HttpServiceProxyFactory factory = HttpServiceProxyFactory
				.builderFor(WebClientAdapter.create(webClient))
				.build();
		return factory.createClient(BrevoEmailClient.class);
	}
}
