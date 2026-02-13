package in.ashar.mooble.service;

import in.ashar.mooble.dto.ParentsDetailDto;
import in.ashar.mooble.dto.RegisterAdminRequest;
import in.ashar.mooble.dto.RegisterStudentRequest;
import in.ashar.mooble.dto.RegisterTeacherRequest;
import in.ashar.mooble.entity.*;
import in.ashar.mooble.exception.AlreadyExists;
import in.ashar.mooble.exception.NotFoundException;
import in.ashar.mooble.repository.*;
import in.ashar.mooble.utility.enums.Role;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class RegisterService {

    private final CredentialsRepository credentialsRepository;
    private final Admin2Repository adminRepository;
    private final Teacher2Repository teacherRepository;
    private final Student2Repository studentRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final RegistrationOtpRepository otpRepository;
    private final ForgotPasswordOtpRepository forgotPasswordOtpRepository;
    private final SubscriptionPlanRepository planRepository;
    private final AdminSubscriptionRepository subscriptionRepository;
    private final AdminUsageRepository usageRepository;



    @Transactional
    public Admin2 createAdmin(@Valid RegisterAdminRequest dto) {

        String email = dto.getAdminEmail();

        if (credentialsRepository.existsByEmail(email)) {
            throw new AlreadyExists("user with same email already exists");
        }

        SubscriptionPlan freePlan = planRepository.findByName("FREE")
                .orElseThrow(() -> new IllegalStateException("FREE plan not configured"));


        Admin2 admin = new Admin2();

        /// Details
        admin.setAdminName(dto.getAdminName());
        admin.setAdminEmail(dto.getAdminEmail());
        admin.setAdminPhoneNumber(dto.getAdminPhoneNumber());
        admin.setAdminAddress(dto.getAdminAddress());

        /// Credentials
        Credentials2 credential = new Credentials2();
        credential.setEmail(dto.getAdminEmail());
        credential.setRole(Role.ADMIN);
        credential.setPassword(passwordEncoder.encode(dto.getAdminPassword()));
        Credentials2 savedCredential = credentialsRepository.save(credential);

        admin.setAdminCredential(savedCredential);

        Admin2 savedAdmin = adminRepository.save(admin);

        // ===== Create subscription =====
        AdminSubscription sub = new AdminSubscription();
        sub.setAdmin(savedAdmin);
        sub.setPlan(freePlan);
        sub.setStartDate(LocalDate.now());
        sub.setEndDate(null);
        sub.setActive(true);
        subscriptionRepository.save(sub);

        // ===== Create usage =====
        AdminUsage usage = new AdminUsage();
        usage.setAdmin(savedAdmin);
        usage.setUsedMembers(1);
        usage.setUsedStorageKb(0);
        usageRepository.save(usage);

        savedAdmin.setSubscription(sub);
        savedAdmin.setUsage(usage);


        return savedAdmin;

    }


    public Teacher2 createTeacher(@Valid RegisterTeacherRequest dto) {

        String email = dto.getTeacherEmail();

        if (credentialsRepository.existsByEmail(email)) {
            throw new AlreadyExists("user with same email already exists");
        }

        /// Details
        Teacher2 teacher = new Teacher2();
        teacher.setTeacherName(dto.getTeacherName());
        teacher.setTeacherPhoneNumber(dto.getTeacherPhoneNumber());
        teacher.setTeacherAddress(dto.getTeacherAddress());
        teacher.setBirthDate(dto.getDateOfBirth());
        teacher.setExperience(dto.getExperience());
        teacher.setKnownSubjects(dto.getKnownSubjects());

        /// Credentials
        Credentials2 credential = new Credentials2();
        credential.setEmail(dto.getTeacherEmail());
        credential.setPassword(passwordEncoder.encode(dto.getTeacherPassword()));
        credential.setRole(Role.TEACHER);
        Credentials2 savedCredential = credentialsRepository.save(credential);

        teacher.setTeacherCredential(savedCredential);

        return teacherRepository.save(teacher);
    }

    public Student2 createStudent(@Valid RegisterStudentRequest dto) {

        String email = dto.getStudentEmail();

        if (credentialsRepository.existsByEmail(email)) {
            throw new AlreadyExists("user with same email already exists");
        }

        /// Details
        Student2 student = new Student2();
        student.setStudentName(dto.getStudentName());
        student.setStudentPhoneNumber(dto.getStudentPhoneNumber());
        student.setStudentAddress(dto.getStudentAddress());
        student.setBirthDate(dto.getDateOfBirth());

        /// Credentials
        Credentials2 credential = new Credentials2();
        credential.setEmail(dto.getStudentEmail());
        credential.setPassword(passwordEncoder.encode(dto.getStudentPassword()));
        credential.setRole(Role.STUDENT);
        Credentials2 savedCredential = credentialsRepository.save(credential);

        /// Parents Detail
        ParentsDetail parentsDetail = new ParentsDetail();
        ParentsDetailDto detail = dto.getParentsDetail();
        parentsDetail.setName(detail.getName());
        parentsDetail.setRelation(detail.getRelation());
        parentsDetail.setAddress(detail.getAddress());
        parentsDetail.setPhone(detail.getPhone());
        parentsDetail.setOccupation(detail.getOccupation());

        student.setStudentCredential(savedCredential);
        student.setParentsDetail(parentsDetail);

        return studentRepository.save(student);

    }


    public void sendOtp(String email) {

        Credentials2 credential = credentialsRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Email not Found"));
        try {
            emailService.sendOtp(credential.getEmail());
        } catch (MessagingException e) {
            log.error(e.getMessage());
        }

    }

    public Map<String, Boolean> verifyOtp(String email, String otp) {

        RegistrationOtp registrationOtp = otpRepository.findByEmailAndOtp(email, otp).orElse(null);

        if (registrationOtp == null || Instant.now().isAfter(registrationOtp.getExpiresAt())) {
            return Map.of("verified", false);
        }

        else{
            registrationOtp.setVerified(true);
            otpRepository.save(registrationOtp);
            return Map.of("verified", true);
        }

    }

    public boolean verifyForgotPasswordOtp(String email, String otp) {

        ForgotPasswordOtp registrationOtp = forgotPasswordOtpRepository.findByEmailAndOtp(email, otp).orElse(null);

        if (registrationOtp == null || Instant.now().isAfter(registrationOtp.getExpiresAt())) {
            return false;
        }

        else{
            forgotPasswordOtpRepository.save(registrationOtp);
            return true;
        }

    }

    public boolean forgotPassword(String email) {

        if(credentialsRepository.existsByEmail(email)){
            try {
                emailService.sendPasswordForgotOtp(email);
                return true;
            } catch (MessagingException e) {
                log.error(e.getMessage());
            }
        }
        return false;

    }


    public boolean changePassword(String email, String otp, String newPassword) {

        ForgotPasswordOtp passwordOtp = forgotPasswordOtpRepository.findByEmailAndOtp(email, otp).orElse(null);

        if(passwordOtp == null) return false;

        Credentials2 credential = credentialsRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("Email not exists"));

        credential.setPassword(passwordEncoder.encode(newPassword));

        credentialsRepository.save(credential);

        return true;

    }
}
