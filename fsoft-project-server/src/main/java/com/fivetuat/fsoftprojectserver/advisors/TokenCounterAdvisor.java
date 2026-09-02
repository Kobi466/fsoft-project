package com.fivetuat.fsoftprojectserver.advisors;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClientRequest;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.api.AdvisorChain;
import org.springframework.ai.chat.client.advisor.api.BaseAdvisor;
import org.springframework.core.Ordered;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class TokenCounterAdvisor implements BaseAdvisor {

	private final AtomicInteger requests = new AtomicInteger();
	private final AtomicInteger promptTokens = new AtomicInteger();
	private final AtomicInteger completionTokens = new AtomicInteger();
	private final AtomicInteger totalTokens = new AtomicInteger();

	@Override
	public int getOrder() {
		return Ordered.LOWEST_PRECEDENCE - 1000;
	}

	@Override
	public ChatClientRequest before(ChatClientRequest request, AdvisorChain chain) {
		return request;
	}

	@Override
	public ChatClientResponse after(ChatClientResponse response, AdvisorChain chain) {
		var chatResponse = response.chatResponse();
		if (chatResponse == null) {
			return response;
		}
		var usage = chatResponse.getMetadata().getUsage();
		this.requests.incrementAndGet();
		this.promptTokens.addAndGet(usage.getPromptTokens());
		this.completionTokens.addAndGet(usage.getCompletionTokens());
		this.totalTokens.addAndGet(usage.getTotalTokens());

		log.info("[AI_LOG] tokens this call: prompt={} completion={} total={} | running total over {} call(s): {}",
				usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens(),
				this.requests.get(), this.totalTokens.get());
		return response;
	}
}
