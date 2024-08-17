package com.sangminlee.mymydata.vo;

import java.time.Instant;

public record Message(
        String messageId,
        String channelId,
        Long sequenceNumber,
        Instant timestamp,
        String author,
        String message,
        int color) {
}
