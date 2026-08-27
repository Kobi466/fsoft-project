package com.fivetuat.fsoftprojectserver.services;

import com.fivetuat.fsoftprojectserver.dtos.request.NotificationCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.NotificationResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface NotificationService {
    NotificationResponse createNotification(NotificationCreationRequest request);
    List<NotificationResponse> getMyNotifications(Pageable pageable);
    long getUnreadCount();
    NotificationResponse markAsRead(Long notificationId);
    void markAllAsRead();
    void deleteNotification(Long notificationId);
}
