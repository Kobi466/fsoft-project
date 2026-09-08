package com.fivetuat.fsoftprojectserver.controllers;

import com.fivetuat.fsoftprojectserver.services.AdminAiChatService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AdminAiChatController.class)
@Import(AdminAiChatControllerSecurityTest.MethodSecurityConfig.class)
class AdminAiChatControllerSecurityTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AdminAiChatService adminAiChatService;

    @TestConfiguration
    @EnableWebSecurity
    @EnableMethodSecurity
    static class MethodSecurityConfig {
        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http.csrf(csrf -> csrf.disable())
                    .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                    .authorizeHttpRequests(auth -> auth.anyRequest().authenticated());
            return http.build();
        }
    }

    @Test
    @WithMockUser(authorities = "ROLE_USER")
    void auditLogsForbiddenForNonAdmin() throws Exception {
        mockMvc.perform(get("/admin/ai/audit-logs"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(authorities = "ROLE_ADMIN")
    void auditLogsAllowedForAdmin() throws Exception {
        mockMvc.perform(get("/admin/ai/audit-logs"))
                .andExpect(status().isOk());
    }
}
