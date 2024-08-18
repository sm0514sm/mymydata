package com.sangminlee.mymydata.repository;

import com.sangminlee.mymydata.vo.Message;
import com.sangminlee.mymydata.vo.NewMessage;
import jakarta.annotation.Nullable;

import java.util.List;

/**
 * 메시지 관리를 위한 저장소 인터페이스입니다.
 * 이 인터페이스는 메시지의 조회, 저장, 삭제 기능을 정의합니다.
 */
public interface MessageRepository {
    /**
     * 특정 채널의 최근 메시지들을 조회합니다.
     *
     * @param channelId         메시지를 조회할 채널의 ID
     * @param fetchMax          조회할 최대 메시지 수
     * @param lastSeenMessageId 마지막으로 본 메시지의 ID (null 가능)
     * @return 조회된 메시지 리스트
     */
    List<Message> findLatest(String channelId, int fetchMax, @Nullable String lastSeenMessageId);

    /**
     * 특정 채널의 최근 메시지들을 조회합니다.
     * 이 메서드는 lastSeenMessageId를 null로 설정하여 findLatest를 호출합니다.
     *
     * @param channelId 메시지를 조회할 채널의 ID
     * @param fetchMax  조회할 최대 메시지 수
     * @return 조회된 메시지 리스트
     */
    default List<Message> findLatest(String channelId, int fetchMax) {
        return findLatest(channelId, fetchMax, null);
    }

    /**
     * 새로운 메시지를 저장합니다.
     *
     * @param newMessage 저장할 새 메시지 객체
     * @return 저장된 메시지 객체
     */
    Message save(NewMessage newMessage);

    /**
     * 특정 채널의 마지막 사용자 메시지를 삭제합니다.
     *
     * @param channelId 메시지를 삭제할 채널의 ID
     */
    void deleteLastUserMessage(String channelId);
}