package com.fivetuat.fsoftprojectserver.services.implement;

import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fivetuat.fsoftprojectserver.dtos.request.NotificationCreationRequest;
import com.fivetuat.fsoftprojectserver.dtos.response.NotificationResponse;
import com.fivetuat.fsoftprojectserver.entities.Notification;
import com.fivetuat.fsoftprojectserver.entities.Profile;
import com.fivetuat.fsoftprojectserver.enums.ErrorCode;
import com.fivetuat.fsoftprojectserver.exceptions.AppException;
import com.fivetuat.fsoftprojectserver.mappers.NotificationMapper;
import com.fivetuat.fsoftprojectserver.repositories.NotificationRepository;
import com.fivetuat.fsoftprojectserver.repositories.ProfileRepository;
import com.fivetuat.fsoftprojectserver.services.NotificationService;
import com.fivetuat.fsoftprojectserver.utils.SecurityUtils;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationServiceImpl implements NotificationService {

    NotificationRepository notificationRepository;
    ProfileRepository profileRepository;
    NotificationMapper notificationMapper;
    SimpMessagingTemplate messagingTemplate;

    @Override
    @Transactional
    @CacheEvict(value = "unread_notifications", key = "#result.recipient.authEmail")
    public NotificationResponse createNotification(NotificationCreationRequest request) {
        log.info("Creating notification for user ID: {}", request.getRecipientId());

        Profile recipient = profileRepository.findById(request.getRecipientId())
                .orElseThrow(() -> new AppException(ErrorCode.PROFILE_NOT_FOUND));

        Notification notification = notificationMapper.toNotification(request);
        notification.setRecipient(recipient);

        Notification savedNotification = notificationRepository.save(notification);
        NotificationResponse response = notificationMapper.toNotificationResponse(savedNotification);

        sendRealTimeNotification(recipient.getId(), response);

        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<NotificationResponse> getMyNotifications(Pageable pageable) {
        UUID currentUserId = getCurrentUserId();
        log.info("Getting notifications for current user ID: {}", currentUserId);

        Page<Notification> notificationPage = notificationRepository
                .findByRecipientIdOrderByCreatedAtDesc(currentUserId, pageable);

        return notificationPage.getContent().stream()
                .map(notificationMapper::toNotificationResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "unread_notifications", key = "T(com.fivetuat.fsoftprojectserver.utils.SecurityUtils).getCurrentUserLogin().orElse(null)")
    public long getUnreadCount() {
        return notificationRepository.countByRecipientIdAndIsReadFalse(getCurrentUserId());
    }

    @Override
    @Transactional
    @CacheEvict(value = "unread_notifications", key = "T(com.fivetuat.fsoftprojectserver.utils.SecurityUtils).getCurrentUserLogin().orElse(null)")
    public NotificationResponse markAsRead(Long notificationId) {
        UUID currentUserId = getCurrentUserId();
        Notification notification = getNotificationWithOwnerCheck(notificationId, currentUserId);

        notification.setRead(true);
        Notification updatedNotification = notificationRepository.save(notification);
        return notificationMapper.toNotificationResponse(updatedNotification);
    }

    @Override
    @Transactional
    @CacheEvict(value = "unread_notifications", key = "T(com.fivetuat.fsoftprojectserver.utils.SecurityUtils).getCurrentUserLogin().orElse(null)")
    public void markAllAsRead() {
        UUID currentUserId = getCurrentUserId();
        List<Notification> unreadNotifications = notificationRepository
                .findByRecipientIdAndIsReadFalse(currentUserId);

        if (!unreadNotifications.isEmpty()) {
            unreadNotifications.forEach(n -> n.setRead(true));
            notificationRepository.saveAll(unreadNotifications);
        }
    }

    @Override
    @Transactional
    @CacheEvict(value = "unread_notifications", key = "T(com.fivetuat.fsoftprojectserver.utils.SecurityUtils).getCurrentUserLogin().orElse(null)")
    public void deleteNotification(Long notificationId) {
        UUID currentUserId = getCurrentUserId();
        Notification notification = getNotificationWithOwnerCheck(notificationId, currentUserId);
        notificationRepository.delete(notification);
    }

    private Notification getNotificationWithOwnerCheck(Long notificationId, UUID currentUserId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new AppException(ErrorCode.NOTIFICATION_NOT_FOUND));

        boolean isRecipient = notification.getRecipient().getId().equals(currentUserId);
        boolean isAdmin = SecurityUtils.isCurrentUserInRole("ROLE_ADMIN");

        if (!isRecipient && !isAdmin) {
            throw new AppException(ErrorCode.ACCESS_DENIED);
        }

        return notification;
    }

    private void sendRealTimeNotification(UUID recipientId, NotificationResponse response) {
        try {
            String destination = "/topic/notifications/" + recipientId;
            messagingTemplate.convertAndSend(destination, response);
            log.info("WebSocket notification sent to topic: {}", destination);
        } catch (Exception e) {
            log.error("Failed to send WebSocket notification to user {}: {}", recipientId, e.getMessage());
        }
    }

    private UUID getCurrentUserId() {
        return SecurityUtils.getCurrentUserLogin()
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHORIZED));
    }
}
