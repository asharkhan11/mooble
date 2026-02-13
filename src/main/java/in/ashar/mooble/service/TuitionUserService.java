//package in.ashar.mooble.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import in.ashar.mooble.configuration.AppProperties;
//import in.ashar.mooble.exception.NotFoundException;
//import in.ashar.mooble.exception.UnAuthorizedException;
//import in.ashar.mooble.security.GetCurrentUser;
//import in.ashar.mooble.utility.enums.Role;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.ArrayList;
//import java.util.List;
//
//@Service
//public class TuitionUserService {
//
//    @Autowired
//    private ObjectMapper objectMapper;
//    @Autowired
//    private PasswordSetupTokenService tokenService;
//
//    @Autowired
//    private EmailService emailService;
//    @Autowired
//    private AppProperties appProperties;
//
//    private final GetCurrentUser getCurrentUser;
//    private final TeacherRepository teacherRepository;
//    private final TuitionRepository tuitionRepository;
//    private final TuitionUserRepository tuitionUserRepository;
//    private final StudentRepository studentRepository;
//
//
//    public TuitionUserService(GetCurrentUser getCurrentUser, TeacherRepository teacherRepository, TuitionRepository tuitionRepository, TuitionUserRepository tuitionUserRepository, StudentRepository studentRepository) {
//        this.getCurrentUser = getCurrentUser;
//        this.teacherRepository = teacherRepository;
//        this.tuitionRepository = tuitionRepository;
//        this.tuitionUserRepository = tuitionUserRepository;
//        this.studentRepository = studentRepository;
//    }
//
//    public List<TuitionUser> getAll(String who) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        Role role;
//        if(who.equals("TEACHER")){
//            role = Role.TEACHER;
//        }else{
//            role = Role.STUDENT;
//        }
//
//        return tuitionUserRepository.findTuitionUsersByAdminEmailAndRole(adminEmail, role);
//    }
//
//    public TuitionUser getById(String who, Long id) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//        TuitionUser tuitionUser = tuitionUserRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("TuitionUser not found"));
//
//        // Check that the logged-in admin owns this tuition
//        if (!tuitionUser.getTuition().getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to view this TuitionUser");
//        }
//
//        // Optional: Validate role
//        if (!tuitionUser.getUser().getRole().toString().equals(who)) {
//            throw new UnAuthorizedException("This user does not have the role: " + who);
//        }
//
//        return tuitionUser;
//    }
//
//    public TuitionUser create(String who, TuitionUserDto tu) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        // Validate that tuition belongs to the current admin
//        Tuition tuition = tuitionRepository.findById(tu.getTuitionId())
//                .orElseThrow(() -> new NotFoundException("Tuition not found"));
//
//        if (!tuition.getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to add users to this tuition");
//        }
//
//        // Validate role
//        if (!tu.getUser().getRole().toString().equals(who)) {
//            throw new UnAuthorizedException("The provided user does not match the role: " + who);
//        }
//
//        User user = objectMapper.convertValue(tu.getUser(), User.class);
//        user.setCreatedAt(LocalDateTime.now());
//
//        TuitionUser tuitionUser = new TuitionUser();
//        tuitionUser.setTuition(tuition);
//        tuitionUser.setUser(user);
//        tuitionUser.setJoinedOn(LocalDateTime.now());
//
//        TuitionUser t = tuitionUserRepository.save(tuitionUser);
//
//        String token = tokenService.createToken(t.getUser().getEmail());
//
//        String link = appProperties.getBaseUrl() + "/auth/set-password?token=" + token;
//        emailService.sendPasswordSetupEmail(t.getUser().getEmail(), link);
//
//        return t;
//    }
//
//    public TuitionUser update(String who, Long id, TuitionUserDto tu) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        TuitionUser existing = tuitionUserRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("TuitionUser not found"));
//
//        // Check ownership
//        if (!existing.getTuition().getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to update this TuitionUser");
//        }
//
//        // Validate role
//        if (!existing.getUser().getRole().toString().equals(who)) {
//            throw new UnAuthorizedException("This user does not have the role: " + who);
//        }
//
//        User user = objectMapper.convertValue(tu.getUser(), User.class);
//
//        // Update only allowed fields
//        existing.setUser(user);
//
//        return tuitionUserRepository.save(existing);
//    }
//
//    public void delete(String who, Long id) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        TuitionUser existing = tuitionUserRepository.findById(id)
//                .orElseThrow(() -> new NotFoundException("TuitionUser not found"));
//
//        // Check ownership
//        if (!existing.getTuition().getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to delete this TuitionUser");
//        }
//
//        // Validate role
//        if (!existing.getUser().getRole().toString().equals(who)) {
//            throw new UnAuthorizedException("This user does not have the role: " + who);
//        }
//
//        tuitionUserRepository.delete(existing);
//    }
//
//    public List<TeacherDto> getAllTeachers(Long tuitionId) {
//
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        // Validate that tuition belongs to the current admin
//        Tuition tuition = tuitionRepository.findById(tuitionId)
//                .orElseThrow(() -> new NotFoundException("Tuition not found"));
//
//        if (!tuition.getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to get Teachers of this tuition");
//        }
//
//        List<Teacher> teachers = teacherRepository.findAllByTuitionUserTuition(tuition);
//
//        List<TeacherDto> teacherDtos = new ArrayList<>();
//
//        for (Teacher teacher : teachers) {
//            TeacherDto teacherDto = getTeacherDto(teacher);
//
//            teacherDtos.add(teacherDto);
//        }
//
//        return teacherDtos;
//    }
//
//    @NotNull
//    private static TeacherDto getTeacherDto(Teacher teacher) {
//        TeacherDto teacherDto = new TeacherDto();
//        teacherDto.setName(teacher.getTuitionUser().getUser().getName());
//        teacherDto.setEmail(teacher.getTuitionUser().getUser().getEmail());
//        teacherDto.setPhone(teacher.getTuitionUser().getUser().getPhoneNumber());
//        teacherDto.setStatus(teacher.getStatus());
//        teacherDto.setExperience(teacher.getExperience());
//        teacherDto.setSubjects(teacher.getSubjects());
//        teacherDto.setImageUrl(teacher.getImageUrl());
//        teacherDto.setClassesHandled(teacher.getClassesHandled());
//        teacherDto.setAddress(teacher.getTuitionUser().getUser().getAddress());
//        teacherDto.setTeacherId(teacher.getTeacherId());
//        teacherDto.setJoinedOn(teacher.getTuitionUser().getJoinedOn());
//        return teacherDto;
//    }
//
//    public Teacher createTeacher(Long tuitionId, TeacherDto teacherDto) {
//
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        // Validate that tuition belongs to the current admin
//        Tuition tuition = tuitionRepository.findById(tuitionId)
//                .orElseThrow(() -> new NotFoundException("Tuition not found"));
//
//        if (!tuition.getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to add Teacher to this tuition");
//        }
//
//        UserDto userDto = new UserDto();
//        userDto.setName(teacherDto.getName());
//        userDto.setEmail(teacherDto.getEmail());
//        userDto.setAddress(teacherDto.getAddress());
//        userDto.setPhoneNumber(teacherDto.getPhone());
//        userDto.setRole(Role.TEACHER);
//
//        TuitionUserDto tuitionUserDto = new TuitionUserDto(tuitionId, userDto);
//
//        TuitionUser tuitionUser = create("TEACHER", tuitionUserDto);
//
//        Teacher teacher = new Teacher();
//        teacher.setTuitionUser(tuitionUser);
//        teacher.setSubjects(teacherDto.getSubjects());
//        teacher.setStatus(teacherDto.getStatus());
//        teacher.setExperience(teacherDto.getExperience());
//        teacher.setImageUrl(teacher.getImageUrl());
//        teacher.setClassesHandled(teacherDto.getClassesHandled());
//
//        return teacherRepository.save(teacher);
//
//    }
//
//    public List<StudentDto> getAllStudents(Long tuitionId) {
//
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        // Validate that tuition belongs to the current admin
//        Tuition tuition = tuitionRepository.findById(tuitionId)
//                .orElseThrow(() -> new NotFoundException("Tuition not found"));
//
//        if (!tuition.getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to get Students of this tuition");
//        }
//
//        List<Student> students = studentRepository.findAllByTuitionUserTuition(tuition);
//
//        List<StudentDto> studentDtos = new ArrayList<>();
//
//        for (Student student : students) {
//            StudentDto studentDto = getStudentDto(student);
//
//            studentDtos.add(studentDto);
//        }
//
//        return studentDtos;
//    }
//
//    private StudentDto getStudentDto(Student student) {
//        StudentDto studentDto = new StudentDto();
//        studentDto.setName(student.getTuitionUser().getUser().getName());
//        studentDto.setEmail(student.getTuitionUser().getUser().getEmail());
//        studentDto.setPhone(student.getTuitionUser().getUser().getPhoneNumber());
//        studentDto.setStatus(student.getStatus());
//        studentDto.setSubjects(student.getSubjects());
//        studentDto.setImageUrl(student.getImageUrl());
//        studentDto.setStandard(student.getStandard());
//        studentDto.setAddress(student.getTuitionUser().getUser().getAddress());
//        studentDto.setStudentId(student.getStudentId());
//        studentDto.setJoinedOn(student.getTuitionUser().getJoinedOn());
//        return studentDto;
//    }
//
//    public Student createStudent(Long tuitionId, StudentDto studentDto) {
//
//
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        // Validate that tuition belongs to the current admin
//        Tuition tuition = tuitionRepository.findById(tuitionId)
//                .orElseThrow(() -> new NotFoundException("Tuition not found"));
//
//        if (!tuition.getAdmin().getEmail().equals(adminEmail)) {
//            throw new UnAuthorizedException("You are not allowed to add Student to this tuition");
//        }
//
//        UserDto userDto = new UserDto();
//        userDto.setName(studentDto.getName());
//        userDto.setEmail(studentDto.getEmail());
//        userDto.setAddress(studentDto.getAddress());
//        userDto.setPhoneNumber(studentDto.getPhone());
//        userDto.setRole(Role.STUDENT);
//
//        TuitionUserDto tuitionUserDto = new TuitionUserDto(tuitionId, userDto);
//
//        TuitionUser tuitionUser = create("STUDENT", tuitionUserDto);
//
//        Student student = new Student();
//        student.setTuitionUser(tuitionUser);
//        student.setSubjects(studentDto.getSubjects());
//        student.setStatus(studentDto.getStatus());
//        student.setImageUrl(student.getImageUrl());
//        student.setStandard(studentDto.getStandard());
//
//        return studentRepository.save(student);
//    }
//
//    public TeacherDto updateTeacher(TeacherDto teacherDto) {
//
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        Teacher teacher = teacherRepository.findById(teacherDto.getTeacherId()).orElseThrow(()-> new NotFoundException("Teacher Not Found"));
//
//        if(!teacher.getTuitionUser().getTuition().getAdmin().getEmail().equals(adminEmail)){
//            throw new UnAuthorizedException("You are not allowed to update Teacher of this tuition");
//        }
//
//        TuitionUser tuitionUser = teacher.getTuitionUser();
//
//        if (teacherDto.getName() != null) {
//            tuitionUser.getUser().setName(teacherDto.getName());
//        }
//        if (teacherDto.getSubjects() != null && !teacherDto.getSubjects().isEmpty()) {
//            teacher.setSubjects(teacherDto.getSubjects());
//        }
//        if (teacherDto.getEmail() != null) {
//            tuitionUser.getUser().setEmail(teacherDto.getEmail());
//        }
//        if (teacherDto.getAddress() != null) {
//            tuitionUser.getUser().setAddress(teacherDto.getAddress());
//        }
//        if (teacherDto.getPhone() != null) {
//            tuitionUser.getUser().setPhoneNumber(teacherDto.getPhone());
//        }
//        if (teacherDto.getStatus() != null) {
//            teacher.setStatus(teacherDto.getStatus());
//        }
//        if (teacherDto.getImageUrl() != null) {
//            teacher.setImageUrl(teacherDto.getImageUrl());
//        }
//        if (teacherDto.getExperience() != null) {
//            teacher.setExperience(teacherDto.getExperience());
//        }
//        if (teacherDto.getClassesHandled() != null && !teacherDto.getClassesHandled().isEmpty()) {
//            teacher.setClassesHandled(teacherDto.getClassesHandled());
//        }
//
//        teacher.setTuitionUser(tuitionUser);
//
//        Teacher saved = teacherRepository.save(teacher);
//        return getTeacherDto(saved);
//
//    }
//
//    public void deleteTeacher(Long teacherId) {
//
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        Teacher teacher = teacherRepository.findById(teacherId).orElseThrow(()-> new NotFoundException("Teacher Not Found"));
//
//        if(!teacher.getTuitionUser().getTuition().getAdmin().getEmail().equals(adminEmail)){
//            throw new UnAuthorizedException("You are not allowed to Delete Teacher of this tuition");
//        }
//
//        teacherRepository.deleteById(teacherId);
//
//    }
//
//    public StudentDto updateStudent(StudentDto studentDto) {
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        Student student = studentRepository.findById(studentDto.getStudentId()).orElseThrow(()-> new NotFoundException("Student Not Found"));
//
//        if(!student.getTuitionUser().getTuition().getAdmin().getEmail().equals(adminEmail)){
//            throw new UnAuthorizedException("You are not allowed to update Student of this tuition");
//        }
//
//        TuitionUser tuitionUser = student.getTuitionUser();
//
//        if (studentDto.getName() != null) {
//            tuitionUser.getUser().setName(studentDto.getName());
//        }
//        if (studentDto.getSubjects() != null && !studentDto.getSubjects().isEmpty()) {
//            student.setSubjects(studentDto.getSubjects());
//        }
//        if (studentDto.getEmail() != null) {
//            tuitionUser.getUser().setEmail(studentDto.getEmail());
//        }
//        if (studentDto.getAddress() != null) {
//            tuitionUser.getUser().setAddress(studentDto.getAddress());
//        }
//        if (studentDto.getPhone() != null) {
//            tuitionUser.getUser().setPhoneNumber(studentDto.getPhone());
//        }
//        if (studentDto.getStatus() != null) {
//            student.setStatus(studentDto.getStatus());
//        }
//        if (studentDto.getImageUrl() != null) {
//            student.setImageUrl(studentDto.getImageUrl());
//        }
//        if (studentDto.getStandard() != null) {
//            student.setStandard(studentDto.getStandard());
//        }
//
//        student.setTuitionUser(tuitionUser);
//
//        Student saved = studentRepository.save(student);
//        return getStudentDto(saved);
//    }
//
//    public void deleteStudent(Long studentId) {
//
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        Student student = studentRepository.findById(studentId).orElseThrow(()-> new NotFoundException("Student Not Found"));
//
//        if(!student.getTuitionUser().getTuition().getAdmin().getEmail().equals(adminEmail)){
//            throw new UnAuthorizedException("You are not allowed to update Student of this tuition");
//        }
//
//        studentRepository.deleteById(studentId);
//    }
//}
