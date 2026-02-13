package in.ashar.mooble.service;

import in.ashar.mooble.dto.BroadCastMessageResponse;
import in.ashar.mooble.dto.BroadcastMessageDTO;
import in.ashar.mooble.dto.BroadcastMessageRequest;
import in.ashar.mooble.dto.UnreadCountsDTO;
import in.ashar.mooble.entity.*;
import in.ashar.mooble.exception.NotFoundException;
import in.ashar.mooble.exception.UnAuthorizedException;
import in.ashar.mooble.repository.*;
import in.ashar.mooble.security.GetCurrentUser;
import in.ashar.mooble.utility.enums.AudienceType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Transactional
@RequiredArgsConstructor
public class BroadcastMessageService {

    private final BroadcastMessageRepository messageRepository;
    private final MessageRecipientRepository recipientRepository;
    private final Student2Repository studentRepository;
    private final Teacher2Repository teacherRepository;
    private final Tuition2Repository tuitionRepository;
    private final NotificationService notificationService; // For push notifications
    private final GetCurrentUser currentUser;
    private final AnnouncementReadRepository announcementReadRepository;
    private final ChatReadRepository chatReadRepository;

    public BroadCastMessageResponse sendBroadcast(BroadcastMessageRequest request) {

        int tuitionId = request.getTuitionId();
        String title = request.getTitle();
        String msg = request.getMessage();
        AudienceType audienceType = request.getAudienceType();
        Boolean isUrgent = request.getIsUrgent();

        Admin2 admin = currentUser.getCurrentAdmin();
        Tuition2 tuition = admin.getAdminTuition().stream().filter(t -> t.getTuitionId() == tuitionId).findAny().orElseThrow(() -> new NotFoundException("Tuition not found"));

        // 1. Create the broadcast message
        BroadcastMessage message = new BroadcastMessage();
        message.setTuitionId(tuition.getTuitionId());
        message.setAdminId(admin.getAdminId());
        message.setTitle(title);
        message.setMessage(msg);
        message.setAudienceType(audienceType);
        message.setIsUrgent(isUrgent);

        BroadcastMessage saved = messageRepository.save(message);

        return toResponse(saved, tuition);
    }

    private List<Integer> getRecipientIds(int tuitionId, AudienceType audienceType) {
        List<Integer> recipientIds = new ArrayList<>();
        Tuition2 tuition = tuitionRepository.findById(tuitionId).orElseThrow(() -> new NotFoundException("tuition not found"));

        switch (audienceType) {
            case STUDENTS:
                recipientIds = tuition.getStudentIds();
                break;
            case TEACHERS:
                recipientIds = tuition.getTeacherIds();
                break;
            case BOTH:
                recipientIds.addAll(tuition.getStudentIds());
                recipientIds.addAll(tuition.getTeacherIds());
                break;
        }

        return recipientIds;
    }

    // Get messages for a specific user
    public List<BroadcastMessageDTO> getMessagesForUser(Object user) {

        if (user instanceof Student2 student) {
            return recipientRepository.findAllByRecipientId(student.getStudentId());
        } else if (user instanceof Teacher2 teacher) {
            return recipientRepository.findAllByRecipientId(teacher.getTeacherId());
        } else {
            throw new UnAuthorizedException("Invalid User role");
        }

    }

    // Mark message as read
    public void markAsRead(int messageId, Object user) {

        int userId = 0;

        if (user instanceof Student2 student) {
            userId = student.getStudentId();
        } else if (user instanceof Teacher2 teacher) {
            userId = teacher.getTeacherId();
        } else {
            throw new UnAuthorizedException("Invalid User role");
        }

        MessageRecipient recipient = recipientRepository
                .findByMessageIdAndRecipientId(messageId, userId)
                .orElseThrow(() -> new NotFoundException("Message not found"));

        recipient.setIsRead(true);
        recipient.setReadAt(LocalDateTime.now());
        recipientRepository.save(recipient);
    }

    // Get unread count for user
    public int getUnreadCount(Object user) {

        int userId = 0;

        if (user instanceof Student2 student) {
            userId = student.getStudentId();
        } else if (user instanceof Teacher2 teacher) {
            userId = teacher.getTeacherId();
        } else {
            throw new UnAuthorizedException("Invalid User role");
        }

        return recipientRepository.countByRecipientIdAndIsRead(userId, false);
    }

    public List<BroadCastMessageResponse> getBroadcastHistoryForTuition(int tuitionId) {

        Object user = currentUser.getLoggedInUser();

        Tuition2 tuition = tuitionRepository.findById(tuitionId).orElseThrow(() -> new NotFoundException("Tuition not found"));

        if (user instanceof Student2 student) {

            if (tuition.getStudentIds().stream().noneMatch(sId -> sId == student.getStudentId())) {
                throw new UnAuthorizedException("Invalid tuition id");
            }

            List<BroadcastMessage> messages = messageRepository.findByTuitionIdAndAudienceTypeIn(tuitionId, List.of(AudienceType.STUDENTS.name(), AudienceType.BOTH.name()));

            return messages.stream().map(m -> toResponse(m, tuition)).toList();


        } else if (user instanceof Teacher2 teacher) {
            if (tuition.getTeacherIds().stream().noneMatch(tId -> tId == teacher.getTeacherId())) {
                throw new UnAuthorizedException("Invalid tuition id");
            }

            List<BroadcastMessage> messages = messageRepository.findByTuitionIdAndAudienceTypeIn(tuitionId, List.of(AudienceType.TEACHERS.name(), AudienceType.BOTH.name()));

            return messages.stream().map(m -> toResponse(m, tuition)).toList();


        } else if (user instanceof Admin2 admin) {
            if (admin.getAdminTuition().stream().noneMatch(t -> t.getTuitionId() == tuitionId)) {
                throw new UnAuthorizedException("Invalid tuition id");
            }

            List<BroadcastMessage> messages = messageRepository.findByTuitionId(tuitionId);

            return messages.stream().map(m -> toResponse(m, tuition)).toList();

        } else {
            throw new UnAuthorizedException("Access Denied");
        }

    }

    public BroadCastMessageResponse toResponse(BroadcastMessage message, Tuition2 tuition) {

        return BroadCastMessageResponse.builder()
                .id(message.getId())
                .tuitionId(tuition.getTuitionId())
                .tuitionName(tuition.getTuitionName())
                .title(message.getTitle())
                .message(message.getMessage())
                .audienceType(message.getAudienceType().name())
                .isUrgent(message.getIsUrgent())
                .announceDate(message.getCreatedAt())
                .build();

    }

    public void deleteBroadCaseMessageById(int id) {

        BroadcastMessage message = messageRepository.findById(id).orElseThrow(() -> new NotFoundException("Message Not Found"));

        if (currentUser.getCurrentAdmin().getAdminId() != message.getAdminId()) {
            throw new UnAuthorizedException("Access Denied");
        }

        messageRepository.deleteById(message.getId());

    }


    /// /////////////////////////

    public UnreadCountsDTO getUnreadCounts(Integer tuitionId) {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Teacher2 teacher) {
            int unreadAnnouncements = countUnreadAnnouncements(teacher.getTeacherId(), tuitionId);
            int unreadChats = countUnreadChats(teacher.getTeacherId(), tuitionId);

            return new UnreadCountsDTO(unreadChats, unreadAnnouncements);
        } else {
            throw new UnAuthorizedException("Access Denied");
        }
    }

    /**
     * Count unread announcements for teacher in tuition
     */
    private int countUnreadAnnouncements(Integer teacherId, Integer tuitionId) {
        // Get all announcements for tuition where audience includes TEACHERS or BOTH
        List<BroadcastMessage> announcements = messageRepository
                .findByTuitionIdAndAudienceTypeIn(
                        tuitionId,
                        List.of("TEACHERS", "BOTH")
                );

        // Get read announcement IDs
        Set<Integer> readAnnouncementIds = new HashSet<>(announcementReadRepository
                .findReadAnnouncementIdsByTeacherIdAndTuitionId(teacherId, tuitionId));

        // Count announcements not in read list
        return (int) announcements.stream()
                .filter(a -> !readAnnouncementIds.contains(a.getId()))
                .count();
    }

    /**
     * Count unread chats for teacher in tuition
     * TODO: Implement when chat functionality is ready
     */
    private int countUnreadChats(Integer teacherId, Integer tuitionId) {
        // Placeholder - implement when chat feature is ready
        // Example logic:
        // 1. Get last read timestamp from ChatRead
        // 2. Count messages after that timestamp

        ChatRead chatRead = chatReadRepository
                .findByTeacherIdAndTuitionId(teacherId, tuitionId)
                .orElse(null);

        if (chatRead == null) {
            // Never read, count all messages
            // return chatRepository.countByTuitionId(tuitionId);
            return 0; // Placeholder
        }

        // Count messages after last read timestamp
        // return chatRepository.countByTuitionIdAndCreatedAtAfter(tuitionId, chatRead.getLastReadAt());
        return 0; // Placeholder
    }

    /**
     * Mark all announcements in tuition as read
     */
    @Transactional
    public void markAnnouncementsAsRead(Integer tuitionId) {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Teacher2 teacher) {
            // Get all announcements for tuition
            List<BroadcastMessage> announcements = messageRepository
                    .findByTuitionIdAndAudienceTypeIn(
                            tuitionId,
                            List.of("TEACHERS", "BOTH")
                    );

            // Mark each as read if not already
            for (BroadcastMessage announcement : announcements) {
                if (!announcementReadRepository.existsByTeacherIdAndAnnouncementId(
                        teacher.getTeacherId(), announcement.getId())) {

                    AnnouncementRead read = new AnnouncementRead();
                    read.setTeacherId(teacher.getTeacherId());
                    read.setAnnouncementId(announcement.getId());
                    read.setTuitionId(tuitionId);
                    announcementReadRepository.save(read);
                }
            }
        } else {
            throw new UnAuthorizedException("Access Denied");
        }

    }

    /**
     * Mark single announcement as read
     */
    @Transactional
    public void markAnnouncementAsRead(Integer announcementId, Integer tuitionId) {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Teacher2 teacher) {

            if (!announcementReadRepository.existsByTeacherIdAndAnnouncementId(
                    teacher.getTeacherId(), announcementId)) {

                AnnouncementRead read = new AnnouncementRead();
                read.setTeacherId(teacher.getTeacherId());
                read.setAnnouncementId(announcementId);
                read.setTuitionId(tuitionId);
                announcementReadRepository.save(read);
            }
        } else {
            throw new UnAuthorizedException("Access Denied");
        }
    }

    /**
     * Mark chats as read for tuition
     */
    @Transactional
    public void markChatsAsRead(Integer tuitionId) {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Teacher2 teacher) {


            ChatRead chatRead = chatReadRepository
                    .findByTeacherIdAndTuitionId(teacher.getTeacherId(), tuitionId)
                    .orElse(new ChatRead());

            chatRead.setTeacherId(teacher.getTeacherId());
            chatRead.setTuitionId(tuitionId);
            chatRead.setLastReadAt(LocalDateTime.now());

            chatReadRepository.save(chatRead);
        } else {
            throw new UnAuthorizedException("Access Denied");
        }
    }
}