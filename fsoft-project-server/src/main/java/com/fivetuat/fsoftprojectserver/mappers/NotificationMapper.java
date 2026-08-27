package com.fivetuat.fsoftprojectserver.mappers;

import com.fivetuat.fsoftprojectserver.dtos.request.NotificationCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.NotificationResponse;
import com.fivetuat.fsoftprojectserver.entities.Notification;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationMapper {
    @Mapping(target = "recipient", ignore = true)
    Notification toNotification(NotificationCreationRequest request);

    @Mapping(target = "recipientId", source = "recipient.id")
    NotificationResponse toNotificationResponse(Notification notification);
}
