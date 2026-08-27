package com.fivetuat.fsoftprojectserver.advisors;

import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.ToolResponseMessage;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.model.tool.ToolCallingChatOptions;

import java.util.List;

@Slf4j
public class AvailableToolsLoggingAdvisor implements BaseAdvisor {

	public static final int DEFAULT_ORDER = 1000;

	private final int order;

	public AvailableToolsLoggingAdvisor() {
		this(DEFAULT_ORDER);
	}

	public AvailableToolsLoggingAdvisor(int order) {
		this.order = order;
	}

	//thứ tự chạy của advisor
	@Override
	public int getOrder() {
		return this.order;
	}

	//log token sau khi llm trả lời
	@Override
	public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
		List<String> toolNames = List.of();
		if (request.prompt().getOptions() instanceof ToolCallingChatOptions options
				&& options.getToolCallbacks() != null) {
			toolNames = options.getToolCallbacks()
				.stream()
				.map(tc -> tc.getToolDefinition().name())
				.sorted()
				.toList();
		}

		log.info(">>> [AI_LOG] LLM call - {} tool(s) visible to the model: {}", toolNames.size(), toolNames);

		Message last = request.prompt().getLastUserOrToolResponseMessage();
		if (last instanceof ToolResponseMessage toolResponse) {
			toolResponse.getResponses()
				.forEach(r -> log.info("      [AI_LOG] tool result [{}]: {}", r.name(), truncate(r.responseData(), 260)));
		}
		return request;
	}

	@Override
	public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
		var chatResponse = response.chatResponse();
		if (chatResponse == null) {
			return response;
		}
		chatResponse.getResults()
			.stream()
			.map(Generation::getOutput)
			.filter(message -> !message.getToolCalls().isEmpty())
			.flatMap(message -> message.getToolCalls().stream())
			.forEach(toolCall -> log.info("      [AI_LOG] model -> calls {}({})", toolCall.name(), truncate(toolCall.arguments(), 160)));
		return response;
	}

	private static String truncate(@Nullable String text, int max) {
		if (text == null) {
			return "";
		}
		return text.length() <= max ? text : text.substring(0, max) + "…";
	}
}
