package in.ashar.mooble.controller;

import in.ashar.mooble.dto.BroadCastMessageResponse;
import in.ashar.mooble.dto.BroadcastMessageRequest;
import in.ashar.mooble.dto.UnreadCountsDTO;
import in.ashar.mooble.service.BroadcastMessageService;
import in.ashar.mooble.utility.helpers.AdminHelper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/announcement")
@RequiredArgsConstructor
public class BroadcastMessageController {

    private final BroadcastMessageService messageService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/send")
    public ResponseEntity<BroadCastMessageResponse> sendBroadcast(
            @RequestBody @Valid BroadcastMessageRequest request) {

        return ResponseEntity.ok(messageService.sendBroadcast(request));
    }


    @GetMapping
    public ResponseEntity<List<BroadCastMessageResponse>> getBroadcastAnnouncements(
            @RequestParam int tuitionId) {

        return ResponseEntity.ok(messageService.getBroadcastHistoryForTuition(tuitionId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBroadCastMessage(@PathVariable  int id){

        messageService.deleteBroadCaseMessageById(id);

        return ResponseEntity.noContent().build();

    }


    @GetMapping("/unread-counts/{tuitionId}")
    public ResponseEntity<UnreadCountsDTO> getUnreadCounts(
            @PathVariable Integer tuitionId) {


        UnreadCountsDTO counts = messageService.getUnreadCounts(tuitionId);

        return ResponseEntity.ok(counts);
    }


    @PostMapping("/{tuitionId}/mark-read")
    public ResponseEntity<Void> markAnnouncementsAsRead(
            @PathVariable Integer tuitionId) {

        messageService.markAnnouncementsAsRead(tuitionId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/chats/{tuitionId}/mark-read")
    public ResponseEntity<Void> markChatsAsRead(
            @PathVariable Integer tuitionId) {

        messageService.markChatsAsRead(tuitionId);

        return ResponseEntity.noContent().build();
    }


    @PostMapping("/announcement/{announcementId}/mark-read")
    public ResponseEntity<Void> markAnnouncementAsRead(
            @PathVariable Integer announcementId,
            @RequestParam Integer tuitionId) {

        messageService.markAnnouncementAsRead(announcementId, tuitionId);

        return ResponseEntity.noContent().build();
    }

}