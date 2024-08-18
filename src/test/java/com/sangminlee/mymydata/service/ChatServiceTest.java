package com.sangminlee.mymydata.service;

import com.sangminlee.mymydata.constant.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    private final String CHANNEL_ID = "testChannel";
    private final String MESSAGE = "Hello, World!";
    private final Instant NOW = Instant.parse("2023-01-01T00:00:00Z");
    @Mock
    private ChannelService channelService;
    @Mock
    private MessageService messageService;
    @Mock
    private Clock clock;
    @InjectMocks
    private ChatService chatService;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(NOW);
    }

    @Test
    @DisplayName("유효한 입력의 메시지 게시 및 저장")
    void postMessageWithValidInputShouldSaveMessageTest() {
        when(channelService.channelNotExists(CHANNEL_ID)).thenReturn(false);

        chatService.postMessage(CHANNEL_ID, MESSAGE, Author.USER);

        verify(messageService).saveMessage(argThat(message ->
                message.channelId().equals(CHANNEL_ID) &&
                        message.message().equals(MESSAGE) &&
                        message.timestamp().equals(NOW) &&
                        message.author() == Author.USER
        ));
    }

    @Test
    @DisplayName("존재하지 않는 채널에 메시지 게시 예외 발생")
    void postMessageToNonExistentChannelShouldThrowExceptionTest() {
        when(channelService.channelNotExists(CHANNEL_ID)).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () ->
                chatService.postMessage(CHANNEL_ID, MESSAGE, Author.USER)
        );

        verify(messageService, never()).saveMessage(any());
    }

}