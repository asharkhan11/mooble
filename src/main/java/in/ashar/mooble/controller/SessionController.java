package in.ashar.mooble.controller;


import in.ashar.mooble.dto.SessionRequest;
import in.ashar.mooble.dto.SessionResponse;
import in.ashar.mooble.dto.SessionUpdate;
import in.ashar.mooble.entity.Session;
import in.ashar.mooble.service.SessionServiceImpl;
import in.ashar.mooble.utility.message.MapObjects;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/session")
@RequiredArgsConstructor
@Validated
public class SessionController {

    private final SessionServiceImpl sessionService;
    private final MapObjects mapObjects;
    

    // Admin creates a session
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<List<SessionResponse>> createSession(@RequestBody SessionRequest request) {
        List<Session> sessions = sessionService.createSession(request);
        return ResponseEntity.ok(sessions.stream().map(mapObjects::mapSessionResponse).toList());
    }

    // use this for student provider
    @GetMapping("/{id}")
    public ResponseEntity<SessionResponse> getSession(@PathVariable int id) {
        Session session = sessionService.getSession(id);
        return ResponseEntity.ok(mapObjects.mapSessionResponse(session));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SessionResponse>> updateSession(@PathVariable int id,
                                                  @RequestBody SessionUpdate request) {
        List<Session> sessions = sessionService.updateSession(id, request);
        return ResponseEntity.ok(sessions.stream().map(mapObjects::mapSessionResponse).toList());
    }

    @DeleteMapping("/{id}/{deleteAll}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSession(@PathVariable int id, @PathVariable boolean deleteAll) {
        sessionService.deleteSession(id, deleteAll);
        return ResponseEntity.noContent().build();
    }


    @GetMapping("/admin")
    public ResponseEntity<List<SessionResponse>> getAdminSessions(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<Session> sessions = sessionService.getAdminSessions(startDate, endDate);
        return ResponseEntity.ok(sessions.stream().map(mapObjects::mapSessionResponse).toList());
    }

    @GetMapping("/teacher")
    public ResponseEntity<List<SessionResponse>> getTeacherSessions(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<Session> sessions = sessionService.getTeacherSessions(startDate, endDate);
        return ResponseEntity.ok(sessions.stream().map(mapObjects::mapSessionResponse).toList());
    }

    @GetMapping("/student")
    public ResponseEntity<List<SessionResponse>> getStudentSessions(@RequestParam LocalDate startDate, @RequestParam LocalDate endDate) {
        List<Session> sessions = sessionService.getStudentSessions(startDate, endDate);
        return ResponseEntity.ok(sessions.stream().map(mapObjects::mapSessionResponse).toList());
    }


}

