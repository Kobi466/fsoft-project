package com.fivetuat.fsoftprojectserver.configurations;

import com.fivetuat.fsoftprojectserver.advisors.AvailableToolsLoggingAdvisor;
import com.fivetuat.fsoftprojectserver.advisors.TokenCounterAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.memory.repository.jdbc.JdbcChatMemoryRepository;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.google.genai.GoogleGenAiChatOptions;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class MultiAiConfig {

    @Bean
    @Primary
    public ChatModel primaryChatModel(OpenAiChatModel openAiChatModel) {
        return openAiChatModel;
    }

    @Bean(name = "groqClient")
    @Primary
    public ChatClient groqChatClient(OpenAiChatModel openAiChatModel) {
        return ChatClient.builder(openAiChatModel)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model("openai/gpt-oss-120b")
                        .maxTokens(4096)
                )
                .defaultAdvisors(
                        new AvailableToolsLoggingAdvisor(),
                        new TokenCounterAdvisor()
                )
                .defaultSystem("Bạn là một chuyên gia ngôn ngữ học. Nhiệm vụ của bạn là đọc hiểu văn bản, "
                        + "trích xuất từ vựng khó theo đúng chủ đề được yêu cầu và trả về kết quả chuẩn định dạng JSON. "
                        + "Tuyệt đối KHÔNG trả lời thêm bất kỳ câu văn tự nhiên nào ngoài chuỗi JSON.")
                .build();
    }

    @Bean(name = "geminiVisionClient")
    public ChatClient geminiChatClient(GoogleGenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(GoogleGenAiChatOptions.builder()
                        .model("gemini-2.5-flash")
                )
                .defaultAdvisors(
                        new AvailableToolsLoggingAdvisor(),
                        new TokenCounterAdvisor()
                )
                .build();
    }

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(30)
                .build();
    }

    @Value("${admin-ai.model:openai/gpt-oss-120b}")
    String adminAiModel;

    static final String ADMIN_AI_SYSTEM_PROMPT =
            "Bạn là trợ lý AI dành riêng cho quản trị viên của hệ thống FSoft Flashcards. "
                    + "Bạn có thể tra cứu dữ liệu hệ thống qua các tool được cung cấp và thực hiện các hành động quản trị. "
                    + "Với các hành động nguy hiểm (duyệt/từ chối/xóa bộ bài, đặt official, đổi roles user), "
                    + "bạn phải gọi tool soạn hành động tương ứng, rồi báo cho admin biết actionId và yêu cầu admin xác nhận. "
                    + "KHÔNG được bịa dữ liệu không có trong kết quả tool. Trả lời bằng ngôn ngữ admin đang dùng.";

    @Bean(name = "adminAiClient")
    public ChatClient adminAiChatClient(OpenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(adminAiModel))
                .defaultSystem(ADMIN_AI_SYSTEM_PROMPT)
                .build();
    }
}
