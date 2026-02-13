package in.ashar.mooble.controller;

import in.ashar.mooble.dto.GradeRequestDto;
import in.ashar.mooble.dto.SubmissionResponseDto;
import in.ashar.mooble.entity.Submission;
import in.ashar.mooble.service.SubmissionService;
import in.ashar.mooble.utility.message.MapObjects;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/submission")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionService submissionService;
    private final MapObjects mapObjects;

    @GetMapping
    public ResponseEntity<List<SubmissionResponseDto>> mySubmissions() {
        List<Submission> submissions = submissionService.getAllSubmission();
        List<SubmissionResponseDto> response = submissions.stream().map(mapObjects::mapSubmissionResponse).toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assignment/{assignmentId}")
    public ResponseEntity<List<SubmissionResponseDto>> getSubmissionsByAssignmentId(@PathVariable int assignmentId) {
        List<Submission> submissions = submissionService.getSubmissionByAssignmentId(assignmentId);
        List<SubmissionResponseDto> response = submissions.stream().map(mapObjects::mapSubmissionResponse).toList();
        return ResponseEntity.ok(response);
    }


}
