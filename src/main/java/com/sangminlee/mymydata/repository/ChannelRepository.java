package com.sangminlee.mymydata.repository;


import com.sangminlee.mymydata.vo.Channel;

import java.util.List;
import java.util.Optional;

public interface ChannelRepository {

    List<Channel> findAll();

    void save(String newChannel);

    Optional<Channel> findById(String channelId);

    boolean exists(String channelId);
}
