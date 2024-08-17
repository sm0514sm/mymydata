package com.sangminlee.mymydata.service;

import com.sangminlee.mymydata.repository.ChannelRepository;
import com.sangminlee.mymydata.vo.Channel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChannelService {

    private final ChannelRepository channelRepository;

    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    public void createChannel(String name) {
        channelRepository.save(name);
    }

    public Optional<Channel> getChannel(String channelId) {
        return channelRepository.findById(channelId);
    }

    public boolean channelNotExists(String channelId) {
        return !channelRepository.exists(channelId);
    }
}
