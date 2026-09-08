package com.fivetuat.fsoftprojectserver.mappers;

import com.fivetuat.fsoftprojectserver.dtos.request.CommentCreateRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.CommentResponse;
import com.fivetuat.fsoftprojectserver.entities.Comment;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "communityPost", ignore = true)
    @Mapping(target = "profile", ignore = true)
    @Mapping(target = "parentComment", ignore = true)
    @Mapping(target = "replies", ignore = true)
    @Mapping(target = "attachmentUrl", ignore = true)
    @Mapping(target = "attachmentName", ignore = true)
    Comment toComment(CommentCreateRequest request);

    @Mapping(target = "communityPostId", source = "communityPost.id")
    @Mapping(target = "profileId", source = "profile.id")
    @Mapping(target = "profileName", source = "profile.fullName")
    @Mapping(target = "profileAvatar", source = "profile.avatar")
    @Mapping(target = "parentCommentId", source = "parentComment.id")
    @Mapping(target = "replies", ignore = true)
    CommentResponse toCommentResponse(Comment comment);
}
