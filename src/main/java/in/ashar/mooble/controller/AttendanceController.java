package in.ashar.mooble.controller;

import in.ashar.mooble.dto.AttendanceCreateRequest;
import in.ashar.mooble.dto.AttendanceResponse;
import in.ashar.mooble.dto.AttendanceUpdateRequest;
import in.ashar.mooble.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;

    /* -----------------------------------------
       OPEN ATTENDANCE (SESSION START)
       ----------------------------------------- */
    @PostMapping("/open")
    public ResponseEntity<AttendanceResponse> openAttendance(
            @Valid @RequestBody AttendanceCreateRequest request
    ) {
        return ResponseEntity.ok(
                attendanceService.openAttendance(request)
        );
    }

    /* -----------------------------------------
       UPDATE ATTENDANCE (DRAFT MODE)
       ----------------------------------------- */
    @PutMapping("/{attendanceId}")
    public ResponseEntity<AttendanceResponse> updateAttendance(
            @PathVariable int attendanceId,
            @Valid @RequestBody AttendanceUpdateRequest request
    ) {
        return ResponseEntity.ok(
                attendanceService.updateAttendance(attendanceId, request)
        );
    }

    /* -----------------------------------------
       FINALIZE ATTENDANCE (LOCK)
       ----------------------------------------- */
    @PostMapping("/{attendanceId}/finalize")
    public ResponseEntity<AttendanceResponse> finalizeAttendance(
            @PathVariable int attendanceId
    ) {
        return ResponseEntity.ok(
                attendanceService.finalizeAttendance(attendanceId)
        );
    }

    /* -----------------------------------------
       GET ATTENDANCE BY SESSION
       ----------------------------------------- */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<AttendanceResponse> getBySession(
            @PathVariable int sessionId
    ) {
        return ResponseEntity.ok(
                attendanceService.getAttendanceBySession(sessionId)
        );
    }
}
