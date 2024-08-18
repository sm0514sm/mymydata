package com.sangminlee.mymydata.repository;


import com.sangminlee.mymydata.vo.Channel;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository {

    /**
     * 모든 채널을 조회합니다.
     *
     * @return 채널 리스트
     */
    List<Channel> findAll();

    /**
     * 새로운 채널을 저장합니다.
     *
     * @param newChannel 새 채널의 이름
     */
    void save(String newChannel);

    /**
     * 지정된 ID의 채널을 조회합니다.
     *
     * @param channelId 조회할 채널의 ID
     * @return 채널 객체를 포함한 Optional
     */
    Optional<Channel> findById(String channelId);

    /**
     * 지정된 ID의 채널이 존재하는지 확인합니다.
     *
     * @param channelId 확인할 채널의 ID
     * @return 채널 존재 여부
     */
    boolean exists(String channelId);
}
