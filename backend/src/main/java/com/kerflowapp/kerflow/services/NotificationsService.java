package com.kerflowapp.kerflow.services;

import com.kerflowapp.kerflow.domain.Notification;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.repositories.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationsService {

    private final NotificationRepository notificationRepository;

    public void markUserNotificationAsRead(User loggedUser, UUID notificationId) {
        changeNotificationReadState(loggedUser, notificationId, true);
    }

    public void markUserNotificationAsUnread(User loggedUser, UUID notificationId) {
        changeNotificationReadState(loggedUser, notificationId, false);
    }

    private void changeNotificationReadState(User loggedUser, UUID notificationId, boolean isRead) {
        Optional<Notification> notificationOpt = notificationRepository.findById(notificationId);
        if (notificationOpt.isPresent()) {
            Notification notification = notificationOpt.get();
            notification.setRead(isRead);
            notificationRepository.save(notification);
        }
    }

    @Transactional
    public void markAllUserNotificationAsRead(User loggedUser) {
        notificationRepository.markAllAsReadForUser(loggedUser.getId());
    }
}
