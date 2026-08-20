package com.fivetuat.fsoftprojectserver.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fivetuat.fsoftprojectserver.dtos.response.ChatMessageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.ReplyMessageResponse;
import com.fivetuat.fsoftprojectserver.entities.ChatMessage;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    @Mapping(target = "senderId", source = "sender.id")
    @Mapping(target = "senderName", expression = "java(getSenderFullName(message))")
    @Mapping(target = "senderAvatar", expression = "java(getSenderAvatarUrl(message))")
    @Mapping(target = "replyTo", source = "replyTo")
    ChatMessageResponse toResponse(ChatMessage message);

    @Mapping(target = "senderName", expression = "java(getReplySenderFullName(reply))")
    ReplyMessageResponse toReplyResponse(ChatMessage reply);

    default String getSenderFullName(ChatMessage message) {
        if (message == null || message.getSender() == null) return "Ẩn danh";
        if (message.getSender().getProfile() != null && message.getSender().getProfile().getFullName() != null) {
            return message.getSender().getProfile().getFullName();
        }
        return message.getSender().getEmail();
    }

    default String getSenderAvatarUrl(ChatMessage message) {
        if (message == null || message.getSender() == null || message.getSender().getProfile() == null) return null;
        return message.getSender().getProfile().getAvatar();
    }

    default String getReplySenderFullName(ChatMessage reply) {
        if (reply == null || reply.getSender() == null) return "Ẩn danh";
        if (reply.getSender().getProfile() != null && reply.getSender().getProfile().getFullName() != null) {
            return reply.getSender().getProfile().getFullName();
        }
        return reply.getSender().getEmail();
    }
}
