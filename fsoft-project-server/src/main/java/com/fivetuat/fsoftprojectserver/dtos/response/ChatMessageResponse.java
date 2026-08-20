package com.fivetuat.fsoftprojectserver.dtos.response;

import java.time.Instant;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ChatMessageResponse {
    String id;
    String senderId;
    String senderName;
    String senderAvatar;
    String content;
    ReplyMessageResponse replyTo;
    Instant createdAt;
}
