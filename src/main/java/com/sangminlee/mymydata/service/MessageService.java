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

@Service
public class MessageService {

    private final MessageRepository messageRepository;
    private final Sinks.Many<Message> sink;

    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
        this.sink = Sinks.many().multicast().directBestEffort();
    }

    public List<Message> getMessageHistory(String channelId, int fetchMax, @Nullable String lastSeenMessageId) {
        return messageRepository.findLatest(channelId, fetchMax, lastSeenMessageId);
    }

    public void saveMessage(NewMessage newMessage) {
        Message savedMessage = messageRepository.save(newMessage);
        sink.tryEmitNext(savedMessage);
    }

    public Flux<List<Message>> getLiveMessages(String channelId) {
        return sink.asFlux()
                .filter(m -> m.channelId().equals(channelId))
                .buffer(Duration.ofMillis(500));
    }

    public void deleteLastUserMessage(String channelId) {
        messageRepository.deleteLastUserMessage(channelId);
        sink.tryEmitNext(new Message("DELETION_NOTIFICATION", channelId, -1L, null, "SYSTEM", "MESSAGE_DELETED", 0));
    }
}