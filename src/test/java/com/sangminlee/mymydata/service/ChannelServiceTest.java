package com.sangminlee.mymydata.service;

import com.sangminlee.mymydata.repository.ChannelRepository;
import com.sangminlee.mymydata.vo.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChannelServiceTest {

    @Mock
    private ChannelRepository channelRepository;

    @InjectMocks
    private ChannelService channelService;

    private Channel testChannel;

    @BeforeEach
    void setUp() {
        testChannel = new Channel("test-id", "Test Channel");
    }

    @Test
    @DisplayName("모든 채널 조회")
    void getAllChannelsShouldReturnListOfChannelsTest() {
        List<Channel> expectedChannels = Arrays.asList(
                new Channel("1", "Channel 1"),
                new Channel("2", "Channel 2")
        );
        when(channelRepository.findAll()).thenReturn(expectedChannels);

        List<Channel> result = channelService.getAllChannels();

        assertEquals(expectedChannels, result);
        verify(channelRepository).findAll();
    }

    @Test
    @DisplayName("새 채널 생성")
    void createChannelShouldSaveToRepositoryTest() {
        String channelName = "New Channel";

        channelService.createChannel(channelName);

        verify(channelRepository).save(channelName);
    }

    @Test
    @DisplayName("존재하는 채널 ID로 조회")
    void getChannelWithExistingIdShouldReturnChannelTest() {
        when(channelRepository.findById(testChannel.id())).thenReturn(Optional.of(testChannel));

        Optional<Channel> result = channelService.getChannel(testChannel.id());

        assertTrue(result.isPresent());
        assertEquals(testChannel, result.get());
        verify(channelRepository).findById(testChannel.id());
    }

    @Test
    @DisplayName("존재하지 않는 채널 ID로 조회")
    void getChannelWithNonExistingIdShouldReturnEmptyOptionalTest() {
        String nonExistingId = "non-existing-id";
        when(channelRepository.findById(nonExistingId)).thenReturn(Optional.empty());

        Optional<Channel> result = channelService.getChannel(nonExistingId);

        assertFalse(result.isPresent());
        verify(channelRepository).findById(nonExistingId);
    }

    @Test
    @DisplayName("존재하지 않는 채널 ID로 exist 확인")
    void channelNotExistsWithNonExistingIdShouldReturnTrueTest() {
        String nonExistingId = "non-existing-id";
        when(channelRepository.exists(nonExistingId)).thenReturn(false);

        boolean result = channelService.channelNotExists(nonExistingId);

        assertTrue(result);
        verify(channelRepository).exists(nonExistingId);
    }

    @Test
    @DisplayName("존재하는 채널 ID로 exist 확인")
    void channelNotExistsWithExistingIdShouldReturnFalseTest() {
        when(channelRepository.exists(testChannel.id())).thenReturn(true);

        boolean result = channelService.channelNotExists(testChannel.id());

        assertFalse(result);
        verify(channelRepository).exists(testChannel.id());
    }
}