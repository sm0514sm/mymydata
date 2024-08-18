package com.sangminlee.mymydata.repository;

import com.sangminlee.mymydata.constant.Author;
import com.sangminlee.mymydata.vo.Message;
import com.sangminlee.mymydata.vo.NewMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class InMemoryMessageRepositoryTest {

    @InjectMocks
    private InMemoryMessageRepository messageRepository;

    @Test
    @DisplayName("빈 저장소에서 최신 메시지 찾기")
    void findLatest_EmptyRepositoryTest() {
        List<Message> messages = messageRepository.findLatest("channel1", 10);
        assertTrue(messages.isEmpty());
    }

    @Test
    @DisplayName("메시지 저장 및 조회")
    void saveAndFindLatestTest() {
        NewMessage newMessage = new NewMessage("channel1", Instant.now(), "Hello", Author.USER);
        Message savedMessage = messageRepository.save(newMessage);

        List<Message> messages = messageRepository.findLatest("channel1", 10);
        assertEquals(1, messages.size());
        assertEquals(savedMessage, messages.getFirst());
    }


    @Test
    @DisplayName("마지막으로 본 메시지 이후의 메시지 조회")
    void findLatestAfterLastSeenMessageTest() {
        for (int i = 0; i < 5; i++) {
            NewMessage newMessage = new NewMessage("channel1", Instant.now(), "Message " + i, Author.USER);
            messageRepository.save(newMessage);
        }

        List<Message> allMessages = messageRepository.findLatest("channel1", 10);
        String lastSeenMessageId = allMessages.get(2).messageId();

        List<Message> newMessages = messageRepository.findLatest("channel1", 10, lastSeenMessageId);
        assertEquals(2, newMessages.size());
        assertEquals("Message 3", newMessages.get(0).message());
        assertEquals("Message 4", newMessages.get(1).message());
    }

    @Test
    @DisplayName("다른 채널의 메시지 분리 확인")
    void separateChannelMessagesTest() {
        messageRepository.save(new NewMessage("channel1", Instant.now(), "Channel 1 Message", Author.USER));
        messageRepository.save(new NewMessage("channel2", Instant.now(), "Channel 2 Message", Author.USER));

        List<Message> channel1Messages = messageRepository.findLatest("channel1", 10);
        List<Message> channel2Messages = messageRepository.findLatest("channel2", 10);

        assertEquals(1, channel1Messages.size());
        assertEquals(1, channel2Messages.size());
        assertEquals("Channel 1 Message", channel1Messages.getFirst().message());
        assertEquals("Channel 2 Message", channel2Messages.getFirst().message());
    }

    @Test
    @DisplayName("마지막 사용자 메시지 삭제")
    void deleteLastUserMessageTest() {
        messageRepository.save(new NewMessage("channel1", Instant.now(), "User Message 1", Author.USER));
        messageRepository.save(new NewMessage("channel1", Instant.now(), "Assistant Message", Author.ASSISTANT));
        messageRepository.save(new NewMessage("channel1", Instant.now(), "User Message 2", Author.USER));

        messageRepository.deleteLastUserMessage("channel1");

        List<Message> messages = messageRepository.findLatest("channel1", 10);
        assertEquals(2, messages.size());
        assertEquals("User Message 1", messages.get(0).message());
        assertEquals("Assistant Message", messages.get(1).message());
    }
}