package com.sangminlee.mymydata.service;

import com.sangminlee.mymydata.constant.Author;
import com.sangminlee.mymydata.vo.NewMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.util.MimeTypeUtils;

import java.time.Clock;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * 채팅 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 사용자 메시지 처리와 AI 응답 생성을 담당합니다.
 */
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChannelService channelService;
    private final MessageService messageService;
    private final ChatClient chatClient;
    private final Clock clock;

    /**
     * 메시지를 처리하고 저장합니다.
     *
     * @param channelId 메시지가 속한 채널 ID
     * @param message   사용자 메시지 내용
     * @throws IllegalArgumentException 지정된 채널이 존재하지 않는 경우
     */
    public void postMessage(String channelId, String message, Author author) {
        if (channelService.channelNotExists(channelId)) {
            throw new IllegalArgumentException("The specified channel does not exist");
        }
        var newMessage = new NewMessage(channelId, clock.instant(), message, author);
        messageService.saveMessage(newMessage);
    }

    /**
     * 사용자 메시지에 대한 AI 응답을 생성하고 저장합니다.
     *
     * @param channelId 메시지가 속한 채널 ID
     * @param message   사용자 메시지 내용
     * @throws IllegalArgumentException 지정된 채널이 존재하지 않는 경우
     */
    public void answerMessage(String channelId, String message, Resource resource) {
        ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt()
                .user(message)
                .advisors(a ->
                        // 채널 ID를 대화 기억의 식별자로 사용
                        a.param(CHAT_MEMORY_CONVERSATION_ID_KEY, channelId)
                                // 대화 컨텍스트로 사용할 이전 메시지의 수 설정
                                .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 100));
        if (resource != null) {
            requestSpec = requestSpec.user(u -> u.text(message).media(MimeTypeUtils.IMAGE_PNG, resource));
        }
        String botAnswer = requestSpec.call().content();
        postMessage(channelId, botAnswer, Author.ASSISTANT);
    }
}
