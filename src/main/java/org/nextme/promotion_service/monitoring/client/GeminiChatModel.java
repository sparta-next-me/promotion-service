package org.nextme.promotion_service.monitoring.client;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.model.Generation;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.messages.AssistantMessage;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GeminiChatModel implements ChatModel {

	private final GeminiClient geminiClient;

	@Override
	public ChatResponse call(Prompt prompt) {
		String promptText = prompt.getContents();
		String responseText = geminiClient.generateContent(promptText);

		AssistantMessage message = new AssistantMessage(responseText);
		Generation generation = new Generation(message);
		return new ChatResponse(java.util.List.of(generation));
	}

	@Override
	public ChatOptions getDefaultOptions() {
		return null;
	}
}
