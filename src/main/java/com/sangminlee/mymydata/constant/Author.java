package com.sangminlee.mymydata.constant;

import lombok.Getter;

@Getter
public enum Author {
    USER("사용자", 0),
    ASSISTANT("마이데이터 어시스턴트 봇", 1);

    private final String name;
    private final int color;

    Author(String name, int color) {
        this.name = name;
        this.color = color;
    }

}