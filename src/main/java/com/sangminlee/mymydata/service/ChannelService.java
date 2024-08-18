package com.sangminlee.mymydata.service;

import com.sangminlee.mymydata.repository.ChannelRepository;
import com.sangminlee.mymydata.vo.Channel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * 채널 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 이 클래스는 채널의 생성, 조회, 존재 여부 확인 등의 기능을 제공합니다.
 */
@Service
public class ChannelService {
    private final ChannelRepository channelRepository;

    /**
     * ChannelService 생성자입니다.
     *
     * @param channelRepository 채널 저장소 인스턴스
     */
    public ChannelService(ChannelRepository channelRepository) {
        this.channelRepository = channelRepository;
    }

    /**
     * 모든 채널을 조회합니다.
     *
     * @return 모든 채널의 리스트
     */
    public List<Channel> getAllChannels() {
        return channelRepository.findAll();
    }

    /**
     * 새로운 채널을 생성합니다.
     *
     * @param name 생성할 채널의 이름
     */
    public void createChannel(String name) {
        channelRepository.save(name);
    }

    /**
     * 특정 ID의 채널을 조회합니다.
     *
     * @param channelId 조회할 채널의 ID
     * @return 조회된 채널을 포함한 Optional 객체
     */
    public Optional<Channel> getChannel(String channelId) {
        return channelRepository.findById(channelId);
    }

    /**
     * 특정 ID의 채널이 존재하지 않는지 확인합니다.
     *
     * @param channelId 확인할 채널의 ID
     * @return 채널이 존재하지 않으면 true, 존재하면 false
     */
    public boolean channelNotExists(String channelId) {
        return !channelRepository.exists(channelId);
    }
}