package com.fivetuat.fsoftprojectserver.configurations;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình OpenAPI (Swagger) cho dự án Fsoft.
 * Lớp này chịu trách nhiệm khởi tạo và cấu hình tài liệu API,
 * bao gồm thông tin chung của dự án và các phương thức xác thực (Security Schemes).
 */
@Configuration
public class OpenApiConfig {

    /**
     * Tạo Bean OpenAPI tùy chỉnh để Springdoc tự động nhận diện và sinh tài liệu.
     *
     * @return Đối tượng OpenAPI đã được cấu hình.
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(apiInfo())
                .components(apiComponents())
                // Áp dụng yêu cầu xác thực "bearerAuth" làm mặc định cho hầu hết các API
                .addSecurityItem(new SecurityRequirement().addList("bearerAuth"));
    }

    /**
     * Cấu hình thông tin chung của API (Tiêu đề, Phiên bản, Mô tả, License...).
     */
    private Info apiInfo() {
        return new Info()
                .title("Fsoft Project API Documentation")
                .version("1.0.0")
                .description("Tài liệu mô tả chi tiết các API của hệ thống Fsoft Project.")
                .license(
                        new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")
                );
    }

    /**
     * Cấu hình các thành phần của OpenAPI, đặc biệt là các phương thức xác thực (Security Schemes).
     */
    private Components apiComponents() {
        return new Components()
                // 1. Cấu hình xác thực bằng JWT (JSON Web Token) cho người dùng hệ thống
                .addSecuritySchemes("bearerAuth",
                        new SecurityScheme()
                                .name("bearerAuth")
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .in(SecurityScheme.In.HEADER)
                                .description("Nhập Access Token (JWT) để xác thực. (Giao diện Swagger đã tự động thêm tiền tố 'Bearer ', bạn chỉ cần nhập chuỗi token).")
                )
                // 2. Cấu hình xác thực bằng Token nội bộ dành cho các service giao tiếp với nhau (vd: fsoft-ai)
                .addSecuritySchemes("internalToken",
                        new SecurityScheme()
                                .name("X-Internal-Token")
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.HEADER)
                                .description("Nhập mã bí mật X-Internal-Token dành riêng cho việc giao tiếp nội bộ giữa các microservices (như fsoft-ai).")
                );
    }
}
