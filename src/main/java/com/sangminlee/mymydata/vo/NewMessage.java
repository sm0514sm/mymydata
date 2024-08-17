package com.sangminlee.mymydata.vo;


import com.sangminlee.mymydata.constant.Author;

import java.time.Instant;

public record NewMessage(String channelId, Instant timestamp, String message, Author author) {
}
