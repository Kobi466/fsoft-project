package com.fivetuat.fsoftprojectserver.configurations;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.google.genai.GoogleGenAiChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
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
				.defaultSystem("Bạn là một chuyên gia ngôn ngữ học. Nhiệm vụ của bạn là đọc hiểu văn bản, " +
						"trích xuất từ vựng khó theo đúng chủ đề được yêu cầu và trả về kết quả chuẩn định dạng JSON. " +
						"Tuyệt đối KHÔNG trả lời thêm bất kỳ câu văn tự nhiên nào ngoài chuỗi JSON.")
				.build();
	}

	@Bean(name = "geminiVisionClient")
	public ChatClient geminiChatClient(GoogleGenAiChatModel chatModel) {
		return ChatClient.builder(chatModel)
				.build();
	}

}
