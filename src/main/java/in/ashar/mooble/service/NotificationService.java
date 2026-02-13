package in.ashar.mooble.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class NotificationService {

    public void sendBroadcastNotifications(List<Integer> recipientIds, String title, String message, Boolean isUrgent) {
    }
}
