package com.fivetuat.fsoftprojectserver.configurations;

import com.fivetuat.fsoftprojectserver.advisors.AvailableToolsLoggingAdvisor;
import com.fivetuat.fsoftprojectserver.advisors.TokenCounterAdvisor;
import com.fivetuat.fsoftprojectserver.tools.UserAiToolRegistry;
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

import java.util.Map;

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

    @Bean(name = {"geminiClient", "geminiVisionClient"})
    public ChatClient geminiChatClient(GoogleGenAiChatModel chatModel) {
        return ChatClient.builder(chatModel)
                .defaultOptions(GoogleGenAiChatOptions.builder()
                        .model("gemini-2.5-flash")
                )
                .defaultAdvisors(
                        new AvailableToolsLoggingAdvisor(),
                        new TokenCounterAdvisor()
                )
                .defaultSystem("Bạn là một chuyên gia ngôn ngữ học AI từ Google. Nhiệm vụ của bạn là đọc hiểu văn bản, "
                        + "xử lý yêu cầu và trả về kết quả chuẩn định dạng JSON. "
                        + "Tuyệt đối KHÔNG bao bọc dữ liệu bằng ký tự markdown hay trả lời thêm câu văn tự nhiên ngoài chuỗi JSON.")
                .build();
    }

    @Bean
    public ChatMemory chatMemory(JdbcChatMemoryRepository repository) {
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(repository)
                .maxMessages(30)
                .build();
    }

    @Value("${admin-ai.model:qwen/qwen3.8-27b}")
    String adminAiModel;

    static final String ADMIN_AI_SYSTEM_PROMPT =
            "Bạn là trợ lý AI dành riêng cho quản trị viên của hệ thống FSoft Flashcards. "
                    + "Bạn có thể tra cứu dữ liệu hệ thống qua các tool được cung cấp và thực hiện các hành động quản trị. "
                    + "Với các hành động nguy hiểm (duyệt/từ chối/xóa bộ bài, đặt official, đổi roles user), "
                    + "bạn phải gọi tool soạn hành động tương ứng, rồi báo cho admin biết actionId và yêu cầu admin xác nhận. "
                    + "KHÔNG được bịa dữ liệu không có trong kết quả tool. Trả lời bằng ngôn ngữ admin đang dùng.";

    @Bean(name = "adminAiClient")
    public ChatClient adminAiChatClient(OpenAiChatModel chatModel) {
        ChatModel sanitizingModel = new ReasoningSanitizingChatModel(chatModel);
        return ChatClient.builder(sanitizingModel)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(adminAiModel)
                        .extraBody(Map.of("include_reasoning", false)))
                .defaultSystem(ADMIN_AI_SYSTEM_PROMPT)
                .build();
    }


    @Value("${user-ai.model:qwen/qwen-2.5-32b}")
    String userAiModel;

    static final String USER_AI_SYSTEM_PROMPT =
            "Bạn là Trợ lý AI Gia sư tiếng Anh thông minh dành cho học viên của hệ thống FSoft Flashcards. "
                    + "Nhiệm vụ của bạn là hỗ trợ học viên giải đáp thắc mắc từ vựng, ngữ pháp, tư vấn phương pháp học hiệu quả và hỗ trợ quản lý bộ bài học tập. "
                    + "Sử dụng các công cụ được cung cấp khi cần thiết để hỗ trợ học viên một cách tốt nhất. "
                    + "Hãy trả lời một cách thân thiện, chính xác, truyền cảm hứng và trình bày đẹp mắt bằng Markdown.";

    @Bean(name = "userAiChatClient")
    public ChatClient userAiChatClient(
            OpenAiChatModel chatModel,
            UserAiToolRegistry toolRegistry) {
        ChatModel sanitizingModel = new ReasoningSanitizingChatModel(chatModel);
        return ChatClient.builder(sanitizingModel)
                .defaultOptions(OpenAiChatOptions.builder()
                        .model(userAiModel)
                        .extraBody(Map.of("include_reasoning", false)))
                .defaultSystem(USER_AI_SYSTEM_PROMPT)
                .defaultTools(toolRegistry.getAllTools())
                .defaultAdvisors(
                        new AvailableToolsLoggingAdvisor(),
                        new TokenCounterAdvisor()
                )
                .build();
    }
}
