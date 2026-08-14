package com.fivetuat.fsoftprojectserver.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.fivetuat.fsoftprojectserver.dtos.response.ChatMessageResponse;
import com.fivetuat.fsoftprojectserver.dtos.response.ReplyMessageResponse;
import com.fivetuat.fsoftprojectserver.entities.ChatMessage;
import com.fivetuat.fsoftprojectserver.entities.authentication.Auth;

@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    @Mapping(target = "senderId", expression = "java(message.getSender() != null ? String.valueOf(message.getSender().getId()) : null)")
    @Mapping(target = "senderName", expression = "java(getDisplayName(message.getSender()))")
    @Mapping(target = "senderAvatar", expression = "java(getAvatar(message.getSender()))")
    @Mapping(target = "replyTo", source = "replyTo")
    ChatMessageResponse toChatMessageResponse(ChatMessage message);

    @Mapping(target = "id", source = "id")
    @Mapping(target = "senderName", expression = "java(getDisplayName(reply.getSender()))")
    @Mapping(target = "content", source = "content")
    ReplyMessageResponse toReplyMessageResponse(ChatMessage reply);

    default String getDisplayName(Auth auth) {
        if (auth == null) return "Ẩn danh";
        if (auth.getProfile() != null && auth.getProfile().getFullName() != null && !auth.getProfile().getFullName().isBlank()) {
            return auth.getProfile().getFullName();
        }
        return auth.getEmail() != null ? auth.getEmail() : "Ẩn danh";
    }

    default String getAvatar(Auth auth) {
        if (auth == null || auth.getProfile() == null) return null;
        return auth.getProfile().getAvatar();
    }
}