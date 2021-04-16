package com.mturski.example.websocket;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonPOJOBuilder;

import lombok.Builder;
import lombok.Data;

/**
 * See https://www.thecuriousdev.org/lombok-builder-with-jackson/
 */
@Data
@JsonDeserialize(builder = ChatMessage.Builder.class)
@Builder(builderClassName = "Builder", toBuilder = true)
public class ChatMessage {
    private final MessageType type;
    private final String content;
    private final String sender;

    public enum MessageType {
        CHAT,
        JOIN,
        LEAVE
    }

    @JsonPOJOBuilder(withPrefix = "")
    public static class Builder { }
}