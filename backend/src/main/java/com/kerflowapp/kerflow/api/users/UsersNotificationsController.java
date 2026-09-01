package com.kerflowapp.kerflow.api.users;

import com.kerflowapp.kerflow.api.autoload.CurrentLoggedUser;
import com.kerflowapp.kerflow.domain.User;
import com.kerflowapp.kerflow.services.NotificationsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/v1/users/{user-id}/notifications")
public class UsersNotificationsController {

    private final NotificationsService notificationsService;

    @PostMapping("/{notification-id}/read")
    public ResponseEntity<Void> markAsRead(@CurrentLoggedUser User loggedUser,
                                           @PathVariable("notification-id") UUID notificationId) {

        notificationsService.markUserNotificationAsRead(loggedUser, notificationId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{notification-id}/unread")
    public ResponseEntity<Void> markAsUnread(@CurrentLoggedUser User loggedUser,
                                             @PathVariable("notification-id") UUID notificationId) {

        notificationsService.markUserNotificationAsUnread(loggedUser, notificationId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/read")
    public ResponseEntity<Void> markAsRead(@CurrentLoggedUser User loggedUser) {
        notificationsService.markAllUserNotificationAsRead(loggedUser);
        return ResponseEntity.ok().build();
    }

}
