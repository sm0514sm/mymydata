package com.sangminlee.mymydata.repository;

import com.sangminlee.mymydata.vo.Channel;
import com.sangminlee.mymydata.vo.Message;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InMemoryChannelRepositoryTest {

    @Mock
    private MessageRepository messageRepository;

    @InjectMocks
    private InMemoryChannelRepository channelRepository;

    @Test
    @DisplayName("빈 저장소에서 모든 채널 조회")
    void findAll_EmptyRepositoryTest() {
        assertTrue(channelRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("채널 저장 후 모든 채널 조회")
    void findAll_WithChannelsTest() {
        channelRepository.save("Channel 1");
        channelRepository.save("Channel 2");

        List<Channel> channels = channelRepository.findAll();
        assertEquals(2, channels.size());
        assertTrue(channels.stream().anyMatch(c -> c.name().equals("Channel 1")));
        assertTrue(channels.stream().anyMatch(c -> c.name().equals("Channel 2")));
    }

    @Test
    @DisplayName("최신 메시지가 있는 채널 조회")
    void findAll_WithLatestMessageTest() {
        channelRepository.save("Channel 1");
        Channel savedChannel = channelRepository.findAll().getFirst();

        Message latestMessage = new Message("msg1", savedChannel.id(), 1L, Instant.now(), "user", "Hello", 0);
        when(messageRepository.findLatest(savedChannel.id(), 1)).thenReturn(List.of(latestMessage));

        List<Channel> channels = channelRepository.findAll();
        assertEquals(1, channels.size());
        Channel channel = channels.getFirst();
        assertEquals("Channel 1", channel.name());
        assertEquals(latestMessage, channel.lastMessage());
    }

    @Test
    @DisplayName("새 채널 저장")
    void saveTest() {
        channelRepository.save("New Channel");

        List<Channel> channels = channelRepository.findAll();
        assertEquals(1, channels.size());
        assertEquals("New Channel", channels.getFirst().name());
    }

    @Test
    @DisplayName("존재하는 채널 ID로 채널 찾기")
    void findById_ExistingChannelTest() {
        channelRepository.save("Channel 1");
        String channelId = channelRepository.findAll().getFirst().id();

        Optional<Channel> foundChannel = channelRepository.findById(channelId);
        assertTrue(foundChannel.isPresent());
        assertEquals("Channel 1", foundChannel.get().name());
    }

    @Test
    @DisplayName("존재하지 않는 채널 ID로 채널 찾기")
    void findById_NonExistentChannelTest() {
        Optional<Channel> foundChannel = channelRepository.findById("non-existent-id");
        assertTrue(foundChannel.isEmpty());
    }

    @Test
    @DisplayName("존재하는 채널 ID 확인")
    void exists_ExistingChannelTest() {
        channelRepository.save("Channel 1");
        String channelId = channelRepository.findAll().getFirst().id();

        assertTrue(channelRepository.exists(channelId));
    }

    @Test
    @DisplayName("존재하지 않는 채널 ID 확인")
    void exists_NonExistentChannelTest() {
        assertFalse(channelRepository.exists("non-existent-id"));
    }
}