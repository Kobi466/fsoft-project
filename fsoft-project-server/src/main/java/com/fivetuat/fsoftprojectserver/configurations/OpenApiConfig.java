package com.fivetuat.fsoftprojectserver.configurations;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
	@Bean
	public OpenAPI customOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("Community Response Platform API")
						.version("1.0")
						.description("Tài liệu hướng dẫn tích hợp các API cho hệ thống Community Response Platform")
						.license(
								new License()
										.name("Apache 2.0")
										.url("http://springdoc.org")
						)
				)
				.addSecurityItem(new SecurityRequirement().addList("bearerAuth"))
				.components(
						new Components()
								.addSecuritySchemes("bearerAuth",
										new SecurityScheme()
												.name("bearerAuth")
												.type(SecurityScheme.Type.HTTP)
												.scheme("bearer")
												.bearerFormat("JWT")
												.in(SecurityScheme.In.HEADER)
												.description("Nhập Bearer token")
								)
				);
	}
}
