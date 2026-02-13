package in.ashar.mooble.controller;

import in.ashar.mooble.dto.BroadcastMessageDTO;
import in.ashar.mooble.entity.Student2;
import in.ashar.mooble.entity.Teacher2;
import in.ashar.mooble.exception.UnAuthorizedException;
import in.ashar.mooble.security.GetCurrentUser;
import in.ashar.mooble.service.BroadcastMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.nio.file.attribute.UserPrincipal;
import java.util.List;

// For students/teachers to view their messages
@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class UserMessageController {

    @Autowired
    private BroadcastMessageService messageService;
    private final GetCurrentUser getCurrentUser;

    @GetMapping("/inbox")
    public ResponseEntity<List<BroadcastMessageDTO>> getMyMessages() {

        Object user = getCurrentUser.getLoggedInUser();

        return ResponseEntity.ok(messageService.getMessagesForUser(user));


    }

    @PutMapping("/{messageId}/read")
    public ResponseEntity<?> markAsRead(@PathVariable int messageId) {

        Object user = getCurrentUser.getLoggedInUser();

        messageService.markAsRead(messageId, user);
        return ResponseEntity.ok(null);
    }

    @GetMapping("/unread-count")
    public ResponseEntity<?> getUnreadCount() {

        Object user = getCurrentUser.getLoggedInUser();

        int count = messageService.getUnreadCount(user);
        return ResponseEntity.ok(count);
    }
}