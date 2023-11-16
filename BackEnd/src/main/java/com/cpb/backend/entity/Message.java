package com.cpb.backend.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@AllArgsConstructor
public class Message {
    @Override
    public String toString() {
        return "{\"type\": " + type + ", \"message\": " + message + "}";
    }

    private int type;

    private String message;
}
