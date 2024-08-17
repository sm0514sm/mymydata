package com.sangminlee.mymydata.service;

import com.sangminlee.mymydata.constant.Author;
import com.sangminlee.mymydata.constant.Author;
import com.sangminlee.mymydata.vo.NewMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.stereotype.Service;

import java.time.Clock;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChannelService channelService;
    private final MessageService messageService;
    private final ChatClient chatClient;
    private final Clock clock;

    public void postMessage(String channelId, String message, Author author) {
        if (channelService.channelNotExists(channelId)) {
            throw new IllegalArgumentException("The specified channel does not exist");
        }
        var newMessage = new NewMessage(channelId, clock.instant(), message, author);
        messageService.saveMessage(newMessage);
    }

    public void answerMessage(String channelId, String message) {
        String botAnswer = chatClient.prompt()
                .user(message)
                .advisors(a -> a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, channelId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100))
                .call()
                .content();
        postMessage(channelId, botAnswer, Author.ASSISTANT);
    }
}
