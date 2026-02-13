package in.ashar.mooble.controller;

import in.ashar.mooble.dto.*;
import in.ashar.mooble.entity.Admin2;
import in.ashar.mooble.entity.Student2;
import in.ashar.mooble.entity.Teacher2;
import in.ashar.mooble.service.RegisterService;
import in.ashar.mooble.utility.message.MapObjects;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Validated
@RestController
@RequestMapping("/api/register")
@RequiredArgsConstructor
public class RegisterController {

    private final RegisterService registerService;
    private final MapObjects mapObjects;

    @PostMapping("/admin")
    public ResponseEntity<Admin2ResponseDto> registerAdmin(@RequestBody @Valid RegisterAdminRequest dto){
        Admin2 admin = registerService.createAdmin(dto);
        return ResponseEntity.ok(mapObjects.mapAdminResponse(admin));
    }


    @PostMapping("/teacher")
    public ResponseEntity<TeacherResponseDto> registerTeacher(@RequestBody @Valid RegisterTeacherRequest dto){
        Teacher2 teacher = registerService.createTeacher(dto);
        return ResponseEntity.ok(mapObjects.mapTeacherResponse(teacher));
    }


    @PostMapping("/student")
    public ResponseEntity<Student2ResponseDto> registerStudent(@RequestBody @Valid RegisterStudentRequest dto){
        Student2 student = registerService.createStudent(dto);
        return ResponseEntity.ok(mapObjects.mapStudentResponse(student));
    }

    @GetMapping("/send-otp")
    public ResponseEntity<Void> sendOtp(@RequestParam String email) {
        registerService.sendOtp(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String,Boolean>> verifyOtp(@RequestParam String email, @RequestParam String otp){
        Map<String, Boolean> response = registerService.verifyOtp(email, otp);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/forgot")
    public ResponseEntity<Boolean> forgotPassword(@RequestParam String email){
        boolean response = registerService.forgotPassword(email);
        return ResponseEntity.ok(response);
    }


    @PostMapping("/forgot/verify-otp")
    public ResponseEntity<Boolean> verifyForgotPasswordOtp(@RequestParam String email, @RequestParam String otp){
        boolean response = registerService.verifyForgotPasswordOtp(email, otp);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Boolean> changePassword(@RequestParam String email, @RequestParam String otp, @RequestParam String newPassword){
        boolean response = registerService.changePassword(email, otp, newPassword);
        return ResponseEntity.ok(response);
    }


}
