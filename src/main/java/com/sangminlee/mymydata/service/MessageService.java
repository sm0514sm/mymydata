package com.sangminlee.mymydata.service;

import com.sangminlee.mymydata.repository.MessageRepository;
import com.sangminlee.mymydata.vo.Message;
import com.sangminlee.mymydata.vo.NewMessage;
import jakarta.annotation.Nullable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.List;

/**
 * 메시지 관리를 위한 서비스 클래스입니다.
 * 이 클래스는 메시지의 저장, 조회, 실시간 스트리밍, 삭제 기능을 제공합니다.
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final Sinks.Many<Message> sink;

    /**
     * MessageService 생성자입니다.
     *
     * @param messageRepository 메시지 저장소 인스턴스
     */
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
        this.sink = Sinks.many().multicast().directBestEffort();
    }

    /**
     * 특정 채널의 메시지 기록을 조회합니다.
     *
     * @param channelId         조회할 채널의 ID
     * @param fetchMax          조회할 최대 메시지 수
     * @param lastSeenMessageId 마지막으로 본 메시지의 ID (null 가능)
     * @return 조회된 메시지 리스트
     */
    public List<Message> getMessageHistory(String channelId, int fetchMax, @Nullable String lastSeenMessageId) {
        return messageRepository.findLatest(channelId, fetchMax, lastSeenMessageId);
    }

    /**
     * 새로운 메시지를 저장하고 실시간 스트림에 발행합니다.
     *
     * @param newMessage 저장할 새 메시지 객체
     */
    public void saveMessage(NewMessage newMessage) {
        Message savedMessage = messageRepository.save(newMessage);
        sink.tryEmitNext(savedMessage);
    }

    /**
     * 특정 채널의 실시간 메시지 스트림을 제공합니다.
     *
     * @param channelId 구독할 채널의 ID
     * @return 메시지 리스트의 Flux 스트림
     */
    public Flux<List<Message>> getLiveMessages(String channelId) {
        return sink.asFlux()
                .filter(m -> m.channelId().equals(channelId))
                .buffer(Duration.ofMillis(500));
    }

    /**
     * 특정 채널의 마지막 사용자 메시지를 삭제하고 삭제 알림을 스트림에 발행합니다.
     *
     * @param channelId 메시지를 삭제할 채널의 ID
     */
    public void deleteLastUserMessage(String channelId) {
        messageRepository.deleteLastUserMessage(channelId);
    }
}