package in.ashar.mooble.utility.helpers;

import com.fasterxml.jackson.databind.ObjectMapper;
import in.ashar.mooble.dto.Student2UpdateDto;
import in.ashar.mooble.dto.Teacher2UpdateDto;
import in.ashar.mooble.entity.*;
import in.ashar.mooble.exception.AlreadyExists;
import in.ashar.mooble.exception.InvalidOptionException;
import in.ashar.mooble.exception.NotFoundException;
import in.ashar.mooble.exception.UnAuthorizedException;
import in.ashar.mooble.repository.*;
import in.ashar.mooble.security.GetCurrentUser;
import in.ashar.mooble.utility.enums.Standard;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class AdminHelper {

    @Autowired
    private ObjectMapper objectMapper;

    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CredentialsRepository credentialsRepository;
    private final GetCurrentUser getCurrentUser;
    private final Tuition2Repository tuitionRepository;
    private final TuitionClassRepository tuitionClassRepository;
    private final Student2Repository studentRepository;
    private final Admin2Repository adminRepository;
    private final Teacher2Repository teacherRepository;
    private final Subject2Repository subjectRepository;
    private final Course2Repository courseRepository;


    public LocalDate convertStringToDate(String date) {

        if (date == null || date.isBlank()) throw new InvalidOptionException("date must not be null");

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return LocalDate.parse(date, formatter);
    }


    public Credentials2 createCredentialWithPassword(String email, String password, List<String> roleNames) {

        List<in.ashar.mooble.entity.Role> roles = roleRepository.findAllByRoleNameIn(roleNames);

        Set<String> foundRoleNames = roles.stream().map(Role::getRoleName).collect(Collectors.toSet());

        List<String> missingRoles = roleNames.stream().filter(r -> !foundRoleNames.contains(r)).toList();

        List<Role> addedRoles = new ArrayList<>();
        for (String r : missingRoles) {
            Role newRole = Role.builder().roleName(r).build();
            addedRoles.add(roleRepository.save(newRole));
        }
        roles.addAll(addedRoles);

        Credentials2 credential = Credentials2.builder().email(email)
                .password(passwordEncoder.encode(password))
                .role(in.ashar.mooble.utility.enums.Role.valueOf(foundRoleNames.iterator().next())).build();

        return credentialsRepository.save(credential);
    }

    public Credentials2 createCredentialWithoutPassword(String email, List<String> roleNames) {

        List<in.ashar.mooble.entity.Role> roles = roleRepository.findAllByRoleNameIn(roleNames);

        Set<String> foundRoleNames = roles.stream().map(Role::getRoleName).collect(Collectors.toSet());

        List<String> missingRoles = roleNames.stream().filter(r -> !foundRoleNames.contains(r)).toList();

        List<Role> addedRoles = new ArrayList<>();
        for (String r : missingRoles) {
            Role newRole = Role.builder().roleName(r).build();
            addedRoles.add(roleRepository.save(newRole));
        }
        roles.addAll(addedRoles);

        Credentials2 credential = Credentials2.builder().email(email).
                role(in.ashar.mooble.utility.enums.Role.valueOf(foundRoleNames.iterator().next())).build();

        return credentialsRepository.save(credential);
    }


    public boolean isEmailExists(String email) {
        return credentialsRepository.existsByEmail(email);
    }


    public Tuition2 getOwnTuitionById(int tuitionId) {

        Tuition2 tuition = tuitionRepository.findById(tuitionId).orElseThrow(() -> new NotFoundException("Tuition not found"));

        if (!tuition.getTuitionAdmin().getAdminEmail().equals(getCurrentUser.getLoggedInUserEmail())) {
            throw new UnAuthorizedException("Invalid Tuition id: " + tuitionId);
        }

        return tuition;

    }


    public TuitionClass createTuitionClass(int tuitionId, Standard standard, char section) {
        Tuition2 tuition = getOwnTuitionById(tuitionId);

        Optional<TuitionClass> optTuitionClass = tuitionClassRepository.findByTuitionAndStandardAndSection(tuition, standard, section);

        if (optTuitionClass.isPresent()) {
            return optTuitionClass.get();
        }

        TuitionClass tuitionClass = TuitionClass.builder().standard(standard).section(section).tuition(tuition).build();

        return tuitionClassRepository.save(tuitionClass);
    }


    public boolean canIUpdateStudent(Student2 student) {

        int studentId = student.getStudentId();

        String currentEmail = getCurrentUser.getLoggedInUserEmail();

        if (currentEmail.equals(student.getStudentCredential().getEmail())) {
            return true;
        }

        Optional<Admin2> optAdmin = adminRepository.findByAdminEmail(currentEmail);

        if (optAdmin.isPresent()) {
            Admin2 admin = optAdmin.get();

            boolean isMyStudent = admin.getAdminTuition().stream()
                    .flatMap(t -> t.getStudentIds().stream()).anyMatch(sId-> sId == studentId);

            if (isMyStudent) {
                return true;
            }
        }

        return false;
    }

    public boolean canIUpdateTeacher(Teacher2 teacher) {

        int teacherId = teacher.getTeacherId();

        String currentEmail = getCurrentUser.getLoggedInUserEmail();

        if (currentEmail.equals(teacher.getTeacherCredential().getEmail())) {
            return true;
        }

        Optional<Admin2> optAdmin = adminRepository.findByAdminEmail(currentEmail);

        if (optAdmin.isPresent()) {
            Admin2 admin = optAdmin.get();

            boolean isMyTeacher = admin.getAdminTuition().stream()
                    .flatMap(t -> t.getTeacherIds().stream()).anyMatch(tId-> tId == teacherId);

            if (isMyTeacher) {
                return true;
            }
        }

        return false;
    }


    public void updateBasicDetailOfStudent(Student2 existingStudent, Student2UpdateDto studentDto) {

        existingStudent.setStudentName(studentDto.getStudentName());
        existingStudent.setStudentAddress(studentDto.getStudentAddress());
        existingStudent.setStudentPhoneNumber(studentDto.getStudentPhoneNumber());
        existingStudent.setBirthDate(studentDto.getDateOfBirth());

        ParentsDetail parentsDetail = objectMapper.convertValue(studentDto.getParentsDetail(), ParentsDetail.class);
        existingStudent.setParentsDetail(parentsDetail);
    }

    public void updateBasicDetailOfTeacher(Teacher2 existingTeacher, Teacher2UpdateDto teacherDto) {

        existingTeacher.setTeacherName(teacherDto.getTeacherName());
        existingTeacher.setTeacherAddress(teacherDto.getTeacherAddress());
        existingTeacher.setTeacherPhoneNumber(teacherDto.getTeacherPhoneNumber());
        existingTeacher.setExperience(teacherDto.getExperience());
        existingTeacher.setBirthDate(teacherDto.getDateOfBirth());
        existingTeacher.setKnownSubjects(teacherDto.getKnownSubjects());

    }

    public void updateCredentialSubjectAndCourseOfStudent(Student2 existingStudent, Student2UpdateDto studentDto) {

        String email = studentDto.getStudentEmail();
        String existingEmail = existingStudent.getStudentCredential().getEmail();

        /// ////////////// credentials ///////////////////

        if (!existingEmail.equals(email)) {
            Optional<Credentials2> optCredential = credentialsRepository.findByEmail(email);

            if (optCredential.isPresent()) {
                throw new AlreadyExists("Email Already exists : " + email);
            }

            Credentials2 credential = existingStudent.getStudentCredential();
            credential.setEmail(email);

            Credentials2 savedCredential = credentialsRepository.save(credential);
            existingStudent.setStudentCredential(savedCredential);
        }

        /////////////// Subjects and Courses /////////////////

        List<Integer> subjectIds = studentDto.getSubjectIds();
        List<Integer> courseIds = studentDto.getCourseIds();

        if(!subjectIds.isEmpty()){

            List<Subject2> subjects = subjectRepository.findAllById(subjectIds);

            existingStudent.getSubjects().clear();
            existingStudent.getSubjects().addAll(subjects);

        }

        if(!courseIds.isEmpty()){

            List<Course2> courses = courseRepository.findAllById(courseIds);

            existingStudent.getCourses().clear();
            existingStudent.getCourses().addAll(courses);

        }

    }


    public void updateCredentialSubjectAndCourseOfTeacher(Teacher2 existingTeacher, Teacher2UpdateDto teacherDto) {

        String email = teacherDto.getTeacherEmail();
        String existingEmail = existingTeacher.getTeacherCredential().getEmail();

        /// ////////////// credentials ///////////////////

        if (!existingEmail.equals(email)) {

            Optional<Credentials2> optCredential = credentialsRepository.findByEmail(email);

            if (optCredential.isPresent()) {
                throw new AlreadyExists("Email Already exists : " + email);
            }

            Credentials2 credential = existingTeacher.getTeacherCredential();
            credential.setEmail(email);

            Credentials2 savedCredential = credentialsRepository.save(credential);
            existingTeacher.setTeacherCredential(savedCredential);
        }
        /////////////// Subjects and Courses /////////////////

        List<Integer> subjectIds = teacherDto.getSubjectIds();
        List<Integer> courseIds = teacherDto.getCourseIds();

        if(!subjectIds.isEmpty()){

            List<Subject2> subjects = subjectRepository.findAllById(subjectIds);

            existingTeacher.getSubjects().clear();
            existingTeacher.getSubjects().addAll(subjects);

        }

        if(!courseIds.isEmpty()){

            List<Course2> courses = courseRepository.findAllById(courseIds);

            existingTeacher.getCourses().clear();
            existingTeacher.getCourses().addAll(courses);

        }

    }


    public Student2 getOwnStudentById(int studentId) {

        Student2 student = studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));

        String adminEmail = getCurrentUser.getLoggedInUserEmail();

        Optional<TuitionClass> any = student.getTuitionClasses().stream().filter(t -> Objects.equals(t.getTuition().getTuitionAdmin().getAdminEmail(), adminEmail)).findAny();
        if (any.isPresent()) return student;
        else throw new UnAuthorizedException("Invalid student id : " + studentId);
    }

    public Teacher2 getOwnTeacherById(int teacherId) {

        Teacher2 teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new NotFoundException("Teacher not found"));

        String adminEmail = getCurrentUser.getLoggedInUserEmail();

        Optional<TuitionClass> any = teacher.getTuitionClasses().stream().filter(t -> Objects.equals(t.getTuition().getTuitionAdmin().getAdminEmail(), adminEmail)).findAny();
        if (any.isPresent()) return teacher;
        else throw new UnAuthorizedException("Invalid Teacher id : " + teacherId);
    }

    public Student2 getAnyStudentByEmail(String email) {
        return studentRepository.findByStudentCredentialEmail(email).orElseThrow(() -> new NotFoundException("Student not found"));
    }

    public Teacher2 getAnyTeacherByEmail(String email) {
        return teacherRepository.findByTeacherCredentialEmail(email).orElseThrow(() -> new NotFoundException("Student not found"));
    }

    public Student2 getOwnStudentByEmail(String email) {
        Student2 student = studentRepository.findByStudentCredentialEmail(email).orElseThrow(() -> new NotFoundException("Student not found"));

        String adminEmail = getCurrentUser.getLoggedInUserEmail();

        Optional<TuitionClass> any = student.getTuitionClasses().stream().filter(t -> Objects.equals(t.getTuition().getTuitionAdmin().getAdminEmail(), adminEmail)).findAny();
        if (any.isPresent()) return student;
        else throw new UnAuthorizedException("Invalid student email : " + email);

    }


    public TuitionClass getOwnTuitionClassById(int tuitionClassId) {
        TuitionClass tuitionClass = tuitionClassRepository.findById(tuitionClassId).orElseThrow(() -> new NotFoundException("Tuition class with id: %d not found".formatted(tuitionClassId)));

        String adminEmail = getCurrentUser.getLoggedInUserEmail();

        if (!tuitionClass.getTuition().getTuitionAdmin().getAdminEmail().equals(adminEmail)) {
            throw new UnAuthorizedException("Invalid tuition class id : " + tuitionClassId);
        }

        return tuitionClass;
    }


}
