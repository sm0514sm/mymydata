package com.sangminlee.mymydata.repository;

import com.sangminlee.mymydata.vo.Message;
import com.sangminlee.mymydata.vo.NewMessage;
import jakarta.annotation.Nullable;

import java.util.List;

public interface MessageRepository {
    List<Message> findLatest(String channelId, int fetchMax, @Nullable String lastSeenMessageId);

    default List<Message> findLatest(String channelId, int fetchMax) {
        return findLatest(channelId, fetchMax, null);
    }

    Message save(NewMessage newMessage);

    void deleteLastUserMessage(String channelId);
}
