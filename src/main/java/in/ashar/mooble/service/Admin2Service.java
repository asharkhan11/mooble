package in.ashar.mooble.service;

import in.ashar.mooble.dto.*;
import in.ashar.mooble.entity.*;
import in.ashar.mooble.exception.AlreadyExists;
import in.ashar.mooble.exception.InvalidOptionException;
import in.ashar.mooble.exception.NotFoundException;
import in.ashar.mooble.exception.UnAuthorizedException;
import in.ashar.mooble.repository.*;
import in.ashar.mooble.security.GetCurrentUser;
import in.ashar.mooble.utility.enums.Role;
import in.ashar.mooble.utility.enums.Standard;
import in.ashar.mooble.utility.helpers.AdminHelper;
import in.ashar.mooble.utility.message.MapObjects;
import jakarta.persistence.EntityManager;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class Admin2Service {

    private final MapObjects mapObjects;
    private final EmailService emailService;
    private final AdminHelper adminHelper;
    private final GetCurrentUser currentUser;
    private final Admin2Repository adminRepository;
    private final Student2Repository studentRepository;
    private final Teacher2Repository teacherRepository;
    private final Subject2Repository subjectRepository;
    private final Course2Repository courseRepository;
    private final Tuition2Repository tuitionRepository;
    private final CredentialsRepository credentialsRepository;
    private final TuitionClassRepository tuitionClassRepository;
    private final TuitionCodeCounterRepository counterRepository;
    private final JoinRequestTuitionRepository requestTuitionRepository;
    private final ResourceFolderRepository2 folderRepository2;
    private final MinioService minioService;
    private final ResourceRepository2 resourceRepository2;
    private final SubscriptionGuardService subscriptionGuardService;
    private final SubscriptionPlanRepository subscriptionPlanRepository;
    private final MessageRecipientRepository messageRecipientRepository;
    private final BroadcastMessageRepository broadcastMessageRepository;
    private final ChatReadRepository chatReadRepository;
    private final AnnouncementReadRepository announcementReadRepository;
    private final EntityManager entityManager;
    private final ClassJoinedRepository classJoinedRepository;

    private final ExecutorService executor = Executors.newFixedThreadPool(3);

    /* ---------------------------
       Existing methods (kept)
       --------------------------- */


    
    public List<Tuition2ResponseDto> getAllTuitionOfAdmin(int adminId) {
        return tuitionRepository
                .findByTuitionAdminAdminId(adminId)
                .stream()
                .map(mapObjects::mapTuitionResponse)
                .toList();
    }


    public Tuition2 getTuitionById(int tuitionId) {
        Tuition2 tuition = tuitionRepository.findById(tuitionId)
                .orElseThrow(() -> new NotFoundException("Tuition not found"));

        if (!tuition.getTuitionAdmin().getAdminEmail()
                .equals(currentUser.getLoggedInUserEmail())) {
            throw new UnAuthorizedException("Invalid Tuition id: " + tuitionId);
        }

        return tuition;
    }


    public List<TuitionClass> getTuitionClassesByIds(List<Integer> tuitionClassIds) {

        List<TuitionClass> tuitionClasses = tuitionClassRepository.findAllById(tuitionClassIds);
        List<Tuition2> adminTuition = currentUser.getCurrentAdminTuition();

        tuitionClasses.forEach(tc -> {
            if (!tc.getTuition().equals(adminTuition)) {
                throw new UnAuthorizedException("Invalid tuition class id : " + tc.getTuitionClassId());
            }
        });

        return tuitionClasses;
    }


    public List<Student2> getAllStudentsInTuition(int tuitionId) {
        Tuition2 tuition = getTuitionById(tuitionId);
        return studentRepository.findAllById(tuition.getStudentIds());
    }


    public List<Student2> getAllStudentsOfAdmin() {

        String adminEmail = currentUser.getLoggedInUserEmail();
        Admin2 admin = getAdmin(adminEmail);
        List<Tuition2> allTuition = admin.getAdminTuition();

        Set<Integer> allStudentIds = allTuition.stream()
                .flatMap(t -> t.getStudentIds().stream())
                .collect(Collectors.toSet());

        return studentRepository.findAllById(new ArrayList<>(allStudentIds));
    }


    public List<Teacher2> getAllTeachersInTuition(int tuitionId) {
        Tuition2 tuition = getTuitionById(tuitionId);
        return teacherRepository.findAllById(tuition.getTeacherIds());
    }


    public List<Teacher2> getAllTeachersOfAdmin() {

        String adminEmail = currentUser.getLoggedInUserEmail();
        Admin2 admin = getAdmin(adminEmail);

        List<Tuition2> allTuition = admin.getAdminTuition();

        Set<Integer> allTeacherIds = allTuition.stream().flatMap(t -> t.getTeacherIds().stream()).collect(Collectors.toSet());

        return teacherRepository.findAllById(new ArrayList<>(allTeacherIds));

    }


    @Transactional
    public Teacher2 createTeacher2(int tuitionId, Teacher2Dto teacher2Dto) {

        Object user = currentUser.getLoggedInUser();

        if (!(user instanceof Admin2 admin)) {
            throw new UnAuthorizedException("Access Denied");
        }

        // ✅ Validate tuition
        Tuition2 tuition = admin.getAdminTuition().stream()
                .filter(t -> t.getTuitionId() == tuitionId)
                .findAny()
                .orElseThrow(() -> new UnAuthorizedException("Invalid Tuition id"));

        String email = teacher2Dto.getTeacherEmail();
        Optional<Credentials2> optCredential = credentialsRepository.findByEmail(email);

        Teacher2 teacher;
        List<Integer> subjectIds = teacher2Dto.getSubjectIds();
        List<Integer> courseIds = teacher2Dto.getCourseIds();

        if (subjectIds.isEmpty() && courseIds.isEmpty()) {
            throw new InvalidOptionException("either subjects or courses must be provided");
        }

        if (optCredential.isEmpty()) {
            // ✅ New teacher flow
            teacher = new Teacher2();
            teacher.setTeacherName(teacher2Dto.getTeacherName());
            teacher.setTeacherAddress(teacher2Dto.getTeacherAddress());
            teacher.setTeacherPhoneNumber(teacher2Dto.getTeacherPhoneNumber());
            teacher.setBirthDate(teacher2Dto.getDateOfBirth());
            teacher.setExperience(teacher2Dto.getExperience());

            // Create new credential with TEACHER role
            Credentials2 credential = adminHelper.createCredentialWithoutPassword(
                    email, Collections.singletonList(Role.TEACHER.name()));
            teacher.setTeacherCredential(credential);

            // ✅ Add subjects
            if (!subjectIds.isEmpty()) {
                List<Subject2> subjects = subjectRepository.findAllById(subjectIds);
                if (subjects.size() != subjectIds.size()) {
                    throw new InvalidOptionException("Invalid Subject ids provided");
                }
                // Ensure all subjects belong to this admin
                if (subjects.stream().anyMatch(s -> s.getTuitionClass().getTuition().getTuitionAdmin().getAdminId() != admin.getAdminId())) {
                    throw new UnAuthorizedException("Invalid subjects provided");
                }
                teacher.getSubjects().addAll(subjects);

                // Add tuition classes linked to these subjects
                List<TuitionClass> tuitionClasses = subjects.stream()
                        .map(Subject2::getTuitionClass)
                        .distinct()
                        .toList();
                teacher.getTuitionClasses().addAll(tuitionClasses);
            }

            // ✅ Add courses
            if (!courseIds.isEmpty()) {
                List<Course2> courses = courseRepository.findAllById(courseIds);
                if (courses.size() != courseIds.size()) {
                    throw new InvalidOptionException("Invalid Course ids provided");
                }
                // Ensure all courses belong to this tuition
                List<Course2> validCourses = courses.stream()
                        .filter(c -> c.getTuition().getTuitionId() == tuitionId)
                        .toList();
                if (validCourses.size() != courses.size()) {
                    throw new UnAuthorizedException("Invalid courses provided");
                }
                teacher.getCourses().addAll(validCourses);
            }

            // ✅ Save and link teacher
            Teacher2 saved = teacherRepository.save(teacher);
            tuition.getTeacherIds().add(saved.getTeacherId());
            tuitionRepository.save(tuition);

            emailService.sendPasswordSetupEmail(email);
            return saved;

        } else {
            // ✅ Existing teacher flow
            Credentials2 credential = optCredential.get();

            teacher = teacherRepository.findByTeacherCredential(credential)
                    .orElseThrow(() -> new AlreadyExists("Some Admin or Student is already using this email"));

            // ✅ Add subjects
            if (!subjectIds.isEmpty()) {
                List<Subject2> subjects = subjectRepository.findAllById(subjectIds);
                if (subjects.size() != subjectIds.size()) {
                    throw new InvalidOptionException("Invalid Subject ids provided");
                }
                if (subjects.stream().anyMatch(s -> s.getTuitionClass().getTuition().getTuitionAdmin().getAdminId() != admin.getAdminId())) {
                    throw new UnAuthorizedException("Invalid subjects provided");
                }
                List<Subject2> newSubjects = subjects.stream()
                        .filter(s -> !teacher.getSubjects().contains(s))
                        .toList();
                teacher.getSubjects().addAll(newSubjects);

                List<TuitionClass> newTuitionClasses = newSubjects.stream()
                        .map(Subject2::getTuitionClass)
                        .filter(tc -> !teacher.getTuitionClasses().contains(tc))
                        .distinct()
                        .toList();
                teacher.getTuitionClasses().addAll(newTuitionClasses);
            }

            // ✅ Add courses
            if (!courseIds.isEmpty()) {
                List<Course2> courses = courseRepository.findAllById(courseIds);
                if (courses.size() != courseIds.size()) {
                    throw new InvalidOptionException("Invalid Course ids provided");
                }
                List<Course2> validCourses = courses.stream()
                        .filter(c -> c.getTuition().getTuitionId() == tuitionId)
                        .toList();
                if (validCourses.size() != courses.size()) {
                    throw new UnAuthorizedException("Invalid courses provided");
                }
                List<Course2> newCourses = validCourses.stream()
                        .filter(c -> !teacher.getCourses().contains(c))
                        .toList();
                teacher.getCourses().addAll(newCourses);
            }

            // ✅ Update tuition linkage
            if (!tuition.getTeacherIds().contains(teacher.getTeacherId())) {
                tuition.getTeacherIds().add(teacher.getTeacherId());
            }

            tuitionRepository.save(tuition);
            return teacherRepository.save(teacher);
        }
    }


    @Transactional
    
    public Student2 createStudent2(int tuitionId, Student2Dto student2Dto) {

        Object user = currentUser.getLoggedInUser();

        if (!(user instanceof Admin2 admin)) {
            throw new UnAuthorizedException("Access Denied");
        }

        // Validate tuition and class
        Tuition2 tuition = admin.getAdminTuition().stream()
                .filter(t -> t.getTuitionId() == tuitionId)
                .findAny()
                .orElseThrow(() -> new UnAuthorizedException("Invalid Tuition id"));

        TuitionClass tuitionClass = tuition.getTuitionClasses().stream()
                .filter(tc -> tc.getTuitionClassId() == student2Dto.getTuitionClassId())
                .findAny()
                .orElseThrow(() -> new UnAuthorizedException("Invalid Tuition class id"));

        List<Integer> subjectIds = student2Dto.getSubjectIds();
        List<Integer> courseIds = student2Dto.getCourseIds();

        if (subjectIds.isEmpty() && courseIds.isEmpty()) {
            throw new InvalidOptionException("either subjects or courses must be provided");
        }

        String email = student2Dto.getStudentEmail();
        Optional<Credentials2> optStudent = credentialsRepository.findByEmail(email);

        Student2 student;

        if (optStudent.isEmpty()) {
            // ✅ New student flow
            student = new Student2();
            student.setStudentName(student2Dto.getStudentName());
            student.setStudentAddress(student2Dto.getStudentAddress());
            student.setStudentPhoneNumber(student2Dto.getStudentPhoneNumber());
            student.setBirthDate(student2Dto.getDateOfBirth());

            // Parents detail mapping
            ParentsDetailDto parentsDto = student2Dto.getParentsDetail();
            ParentsDetail parentsDetail = new ParentsDetail();
            parentsDetail.setName(parentsDto.getName());
            parentsDetail.setAddress(parentsDto.getAddress());
            parentsDetail.setPhone(parentsDto.getPhone());
            parentsDetail.setRelation(parentsDto.getRelation());
            parentsDetail.setOccupation(parentsDto.getOccupation());
            student.setParentsDetail(parentsDetail);

            // Create new credential with STUDENT role
            Credentials2 credential = adminHelper.createCredentialWithoutPassword(
                    email, Collections.singletonList(Role.STUDENT.name()));
            student.setStudentCredential(credential);

            // Add class
            student.getTuitionClasses().add(tuitionClass);

            //Link joined date with class
            student.getClassJoined().add(new ClassJoined(tuitionId, LocalDateTime.now()));

            // Add subjects
            if (!subjectIds.isEmpty()) {
                List<Subject2> subjects = tuitionClass.getSubjects().stream()
                        .filter(s -> subjectIds.contains(s.getSubjectId()))
                        .toList();
                if (subjects.size() != subjectIds.size()) {
                    throw new InvalidOptionException("Invalid Subject ids provided");
                }
                student.getSubjects().addAll(subjects);
            }

            // Add courses
            if (!courseIds.isEmpty()) {
                List<Course2> fetchedCourses = courseRepository.findAllById(courseIds);
                if (fetchedCourses.size() != courseIds.size()) {
                    throw new InvalidOptionException("Invalid Course ids provided");
                }
                List<Course2> courses = fetchedCourses.stream()
                        .filter(c -> c.getTuition().getTuitionId() == tuitionId)
                        .toList();
                if (courses.size() != fetchedCourses.size()) {
                    throw new UnAuthorizedException("Invalid course ids");
                }
                student.getCourses().addAll(courses);
            }

            // Save student and update tuition
            Student2 saved = studentRepository.save(student);
            tuition.getStudentIds().add(saved.getStudentId());
            tuitionRepository.save(tuition);

            emailService.sendPasswordSetupEmail(email);

            return saved;

        } else {
            // ✅ Existing student flow
            Credentials2 credential = optStudent.get();

            student = studentRepository.findByStudentCredential(credential)
                    .orElseThrow(() -> new AlreadyExists("Some Admin or Teacher is already using this email"));

            // Add class if missing
            if (!student.getTuitionClasses().contains(tuitionClass)) {
                student.getTuitionClasses().add(tuitionClass);
                //Link joined date with class
                student.getClassJoined().add(new ClassJoined(tuitionId, LocalDateTime.now()));
            }

            // Add subjects
            if (!subjectIds.isEmpty()) {
                List<Subject2> subjects = tuitionClass.getSubjects().stream()
                        .filter(s -> subjectIds.contains(s.getSubjectId()))
                        .toList();
                if (subjects.size() != subjectIds.size()) {
                    throw new InvalidOptionException("Invalid Subject ids provided");
                }
                List<Subject2> studentSubjects = student.getSubjects();
                List<Subject2> newSubjects = subjects.stream()
                        .filter(s -> !studentSubjects.contains(s))
                        .toList();
                student.getSubjects().addAll(newSubjects);
            }

            // Add courses
            if (!courseIds.isEmpty()) {
                List<Course2> fetchedCourses = courseRepository.findAllById(courseIds);
                if (fetchedCourses.size() != courseIds.size()) {
                    throw new InvalidOptionException("Invalid Course ids provided");
                }
                List<Course2> courses = fetchedCourses.stream()
                        .filter(c -> c.getTuition().getTuitionId() == tuitionId)
                        .toList();
                if (courses.size() != fetchedCourses.size()) {
                    throw new UnAuthorizedException("Invalid course ids");
                }
                List<Course2> studentCourses = student.getCourses();
                List<Course2> newCourses = courses.stream()
                        .filter(c -> !studentCourses.contains(c))
                        .toList();
                student.getCourses().addAll(newCourses);
            }

            // Update tuition if not already linked
            if (!tuition.getStudentIds().contains(student.getStudentId())) {
                tuition.getStudentIds().add(student.getStudentId());
            }

            tuitionRepository.save(tuition);
            return studentRepository.save(student);
        }
    }


    @Transactional

    public Student2 updateStudent(int studentId, Student2UpdateDto studentDto) {

        Student2 existingStudent = studentRepository.findById(studentId).orElseThrow(() -> new NotFoundException("Student with id : " + studentId + " not found"));

        boolean canI = adminHelper.canIUpdateStudent(existingStudent);

        if (!canI) {
            throw new UnAuthorizedException("Invalid student id : " + studentId);
        }

        adminHelper.updateBasicDetailOfStudent(existingStudent, studentDto);
        adminHelper.updateCredentialSubjectAndCourseOfStudent(existingStudent, studentDto);

        return studentRepository.save(existingStudent);
    }


    @Transactional
    
    public void removeStudentFromTuition(int tuitionId, int studentId) {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Admin2 admin) {

            Tuition2 tuition = admin.getAdminTuition().stream().filter(t -> t.getTuitionId() == tuitionId).findAny().orElse(null);

            if (tuition == null) {
                throw new UnAuthorizedException("Invalid Tuition id");
            }

            Student2 student = studentRepository.findById(studentId).orElseThrow(() -> new NotFoundException("Student not found"));

            boolean belongsToTuition = student.getTuitionClasses().stream()
                    .anyMatch(tc -> tc.getTuition().getTuitionId() == tuitionId);
            if (!belongsToTuition) {
                throw new UnAuthorizedException("Student does not belong to this tuition");
            }

            /// remove from tuition
            tuition.getStudentIds().removeIf(id -> Objects.equals(id, student.getStudentId()));

            /// remove subjects and courses
            student.getSubjects().removeIf(s -> s.getTuitionClass().getTuition().getTuitionId() == tuitionId);
            student.getCourses().removeIf(c -> c.getTuition().getTuitionId() == tuitionId);

            /// remove from tuition classes
            List<TuitionClass> removeFrom = student.getTuitionClasses().stream().filter(tc -> tc.getTuition().getTuitionId() == tuitionId).toList();
            removeFrom.forEach(tc -> {
                tc.getStudents().remove(student);
            });

            student.getClassJoined().removeIf(cj -> cj.getTuitionId() == tuitionId);

            tuitionRepository.save(tuition);


            /// remove tuition classes of tuition from student
            student.getTuitionClasses().removeAll(removeFrom);

            subscriptionGuardService.onMemberRemoved(admin);

            studentRepository.save(student);
        }


    }


    @Transactional
    public void removeTeacherFromTuition(int tuitionId, int teacherId) {

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Admin2 admin) {

            // ✅ Validate tuition
            Tuition2 tuition = admin.getAdminTuition().stream()
                    .filter(t -> t.getTuitionId() == tuitionId)
                    .findAny()
                    .orElse(null);

            if (tuition == null) {
                throw new UnAuthorizedException("Invalid Tuition id");
            }

            // ✅ Validate teacher
            Teacher2 teacher = teacherRepository.findById(teacherId)
                    .orElseThrow(() -> new NotFoundException("Teacher not found"));

            // ✅ Ensure teacher belongs to this tuition
            boolean belongsToTuition = teacher.getTuitionClasses().stream()
                    .anyMatch(tc -> tc.getTuition().getTuitionId() == tuitionId);
            if (!belongsToTuition) {
                throw new UnAuthorizedException("Teacher does not belong to this tuition");
            }

            // ✅ Remove from tuition
            tuition.getTeacherIds().removeIf(id -> Objects.equals(id, teacher.getTeacherId()));

            // ✅ Remove subjects and courses related to this tuition
            teacher.getSubjects().removeIf(s -> s.getTuitionClass().getTuition().getTuitionId() == tuitionId);
            teacher.getCourses().removeIf(c -> c.getTuition().getTuitionId() == tuitionId);

            // ✅ Remove from tuition classes
            List<TuitionClass> removeFrom = teacher.getTuitionClasses().stream()
                    .filter(tc -> tc.getTuition().getTuitionId() == tuitionId)
                    .toList();
            removeFrom.forEach(tc -> tc.getTeachers().remove(teacher));

            tuitionRepository.save(tuition);

            // ✅ Remove tuition classes of this tuition from teacher
            teacher.getTuitionClasses().removeAll(removeFrom);

            teacherRepository.save(teacher);

            subscriptionGuardService.onMemberRemoved(admin);

        } else {
            throw new UnAuthorizedException("Access Denied");
        }
    }


    /* ---------------------------
       New admin operations
       --------------------------- */

    // --------- Tuition CRUD ----------


    @Transactional

    public Tuition2ResponseDto updateTuition(int tuitionId, Tuition2UpdateDto tuitionDto) {

        Tuition2 existing = getTuitionById(tuitionId);

        if (!tuitionDto.getTuitionName().isBlank()) {
            existing.setTuitionName(tuitionDto.getTuitionName());
        }
        if (!tuitionDto.getTuitionEmail().isBlank()) {
            existing.setTuitionEmail(tuitionDto.getTuitionEmail());
        }
        if (!tuitionDto.getTuitionPhoneNumber().isBlank()) {
            existing.setTuitionPhoneNumber(tuitionDto.getTuitionPhoneNumber());
        }
        if (!tuitionDto.getTuitionAddress().isBlank()) {
            existing.setTuitionAddress(tuitionDto.getTuitionAddress());
        }
        if (!tuitionDto.getBranch().isBlank()) {
            existing.setBranch(tuitionDto.getBranch());
        }

        // do not overwrite admin or classes here
        return mapObjects.mapTuitionResponse(tuitionRepository.save(existing));
    }

    // --------- TuitionClass CRUD ----------
    @Transactional
    
    public TuitionClassResponseDto createTuitionClass(TuitionClassDto tuitionClassDto) {

        Standard standard = tuitionClassDto.getStandard();
        Tuition2 tuition = getTuitionById(tuitionClassDto.getTuitionId());
        char section = tuitionClassDto.getSection();

        Optional<TuitionClass> tuitionClass = tuitionClassRepository.findByTuitionAndStandardAndSection(tuition, standard, section);

        if (tuitionClass.isPresent()) {
            throw new AlreadyExists(standard.name() + " Class with same standard already exists");
        }

        TuitionClass tc = new TuitionClass();
        tc.setStandard(standard);
        tc.setSection(tuitionClassDto.getSection());
        tc.setTuition(tuition);
        tc.setStudents(new ArrayList<>());
        tc.setTeachers(new ArrayList<>());
        tc.setSubjects(new ArrayList<>());
//        tc.setCourses(new ArrayList<>());


        // add to tuition
        List<TuitionClass> classes = Optional.ofNullable(tuition.getTuitionClasses()).orElse(new ArrayList<>());
        classes.add(tc);
        tuition.setTuitionClasses(classes);
        Tuition2 save = tuitionRepository.save(tuition);
        List<TuitionClass> tuitionClasses = save.getTuitionClasses();
        TuitionClass saved = tuitionClasses.get(tuitionClasses.size() - 1);

        return mapObjects.mapTuitionClassResponse(saved);
    }


    public TuitionClass getTuitionClassById(int classId) {

        TuitionClass tuitionClass = tuitionClassRepository.findById(classId).orElseThrow(() -> new NotFoundException("TuitionClass not found"));

        Tuition2 tuition = tuitionClass.getTuition();
        if (!tuition.getTuitionAdmin().getAdminEmail().equals(currentUser.getLoggedInUserEmail())) {
            throw new UnAuthorizedException("Invalid tuition class id : " + classId);
        }

        return tuitionClass;
    }


    public List<TuitionClassResponseDto> getAllTuitionClassesOfTuition(int tuitionId) {
        Tuition2 tuition = getTuitionById(tuitionId);
        return tuition.getTuitionClasses().stream()
                .map(mapObjects::mapTuitionClassResponse)
                .toList();
    }


    @Transactional
    
    public TuitionClass updateTuitionClass(int classId, TuitionClassUpdateDto dto) {

        TuitionClass tc = getTuitionClassById(classId);
        Tuition2 tuition = tc.getTuition();

        // 1. Validate duplicate standard + section (excluding self)
        boolean duplicateExists = tuition.getTuitionClasses().stream().anyMatch(c -> c.getTuitionClassId() != classId && c.getStandard() == dto.getStandard() && c.getSection() == dto.getSection());

        if (duplicateExists) {
            throw new AlreadyExists("Tuition class with same standard: " + dto.getStandard().name() + " and section: " + dto.getSection() + " already exists. Try changing section.");
        }

        // 2. Update standard and section

        tc.setStandard(dto.getStandard());
        tc.setSection(dto.getSection());

        // 3. Update teachers
        if (dto.getTeacherIds() != null && !dto.getTeacherIds().isEmpty()) {
            List<Teacher2> teachers = teacherRepository.findAllById(dto.getTeacherIds());
            tc.getTeachers().clear();
            tc.getTeachers().addAll(teachers);

            // maintain owning side (if Teacher2 is the owner)
            for (Teacher2 teacher : teachers) {
                List<TuitionClass> tcs = Optional.ofNullable(teacher.getTuitionClasses()).orElse(new ArrayList<>());
                if (!tcs.contains(tc)) {
                    tcs.add(tc);
                }
                teacher.setTuitionClasses(tcs);
            }
            teacherRepository.saveAll(teachers);
        }


        // 4. Update students
        if (dto.getStudentIds() != null && !dto.getStudentIds().isEmpty()) {
            List<Student2> students = studentRepository.findAllById(dto.getStudentIds());
            tc.getStudents().clear();
            tc.getStudents().addAll(students);

            for (Student2 student : students) {
                List<TuitionClass> tcs = Optional.ofNullable(student.getTuitionClasses()).orElse(new ArrayList<>());
                if (!tcs.contains(tc)) {
                    tcs.add(tc);
                }
                student.setTuitionClasses(tcs);
            }
            studentRepository.saveAll(students);
        }


        // 5. Update subjects (One-to-Many, TuitionClass owns them)
        if (dto.getSubjectIds() != null && !dto.getSubjectIds().isEmpty()) {
            List<Subject2> subjects = subjectRepository.findAllById(dto.getSubjectIds());

            Optional<Subject2> any = subjects.stream().filter(s -> !tc.getSubjects().contains(s)).findAny();

            if (any.isPresent()) {
                throw new UnAuthorizedException("Invalid Subject id : " + any.get().getSubjectId());
            }

            // Clear old subjects that were linked
            tc.getSubjects().clear();
            tc.getSubjects().addAll(subjects);
        }

        // 7. Save and return
        return tuitionClassRepository.save(tc);
    }


    @Transactional
    public void deleteTuitionClass(int classId) {
        TuitionClass tc = getTuitionClassById(classId);
        Tuition2 tuition = tc.getTuition();

        // 1️⃣ Unlink from subjects
        for (Subject2 subject : tc.getSubjects()) {
            // unlink this class from subjects
            subject.setTuitionClass(null);
            subjectRepository.save(subject);

            // unlink subject from students
            List<Student2> students = studentRepository.findAllBySubjectsContaining(subject);
            for (Student2 student : students) {
                student.getSubjects().remove(subject);
                studentRepository.save(student);
            }

            // unlink subject from teachers
            List<Teacher2> teachers = teacherRepository.findAllBySubjectsContaining(subject);
            for (Teacher2 teacher : teachers) {
                teacher.getSubjects().remove(subject);
                teacherRepository.save(teacher);
            }
        }

        // 2️⃣ Unlink class from students
        for (Student2 student : tc.getStudents()) {
            student.getTuitionClasses().remove(tc);
            studentRepository.save(student);
        }

        // 3️⃣ Unlink class from teachers
        for (Teacher2 teacher : tc.getTeachers()) {
            teacher.getTuitionClasses().remove(tc);
            teacherRepository.save(teacher);
        }

        // 4️⃣ Remove class from tuition
        if (tuition != null) {
            tuition.getTuitionClasses().remove(tc);
            tuitionRepository.save(tuition);
        }

        // 5️⃣ Finally delete the class
        tuitionClassRepository.delete(tc);
    }


    // --------- Student operations ----------


    @Transactional
    public Student2 addStudentToTuitionClass(String studentEmail, int tuitionClassId) {

        TuitionClass tuitionClass = adminHelper.getOwnTuitionClassById(tuitionClassId);
        Student2 student = adminHelper.getAnyStudentByEmail(studentEmail);
        Tuition2 tuition = tuitionClass.getTuition();

        // Ensure initialized
        if (tuitionClass.getStudents() == null) {
            tuitionClass.setStudents(new ArrayList<>());
        }
        if (tuition.getStudentIds() == null) {
            tuition.setStudentIds(new ArrayList<>());
        }
        if (student.getTuitionClasses() == null) {
            student.setTuitionClasses(new ArrayList<>());
        }

        // Check duplicate
        if (tuitionClass.getStudents().contains(student)) {
            throw new AlreadyExists("Student already exists in this class");
        }

        // Add both sides
        tuitionClass.getStudents().add(student);
        tuition.getStudentIds().add(student.getStudentId());
        student.getTuitionClasses().add(tuitionClass);

//        tuitionClassRepository.save(tuitionClass);
        tuitionRepository.save(tuition);
        return studentRepository.save(student);

    }

    @Transactional
    public void removeStudentFromClass(int studentId, int classId) {
        TuitionClass tc = getTuitionClassById(classId);
        Student2 student = adminHelper.getOwnStudentById(studentId);
        Tuition2 tuition = tc.getTuition();

        // Defensive null checks
        if (tc.getStudents() != null) {
            tc.getStudents().removeIf(s -> Objects.equals(s.getStudentId(), studentId));
        }

        if (student.getTuitionClasses() != null) {
            student.getTuitionClasses().removeIf(t -> Objects.equals(t.getTuitionClassId(), classId));
        }

        if (tuition.getStudentIds() != null) {
            tuition.getStudentIds().removeIf(sId -> Objects.equals(sId, studentId));
        }

//        tuitionClassRepository.save(tc); // tution classes field has cascade all
        tuitionRepository.save(tc.getTuition());
        studentRepository.save(student);

        subscriptionGuardService.onMemberRemoved(tuition.getTuitionAdmin());

    }

    // --------- Teacher operations ----------

    public Teacher2 getTeacherById(int teacherId) {
        return teacherRepository.findById(teacherId).orElseThrow(() -> new NotFoundException("Teacher not found"));
    }

    @Transactional
    public Teacher2 updateTeacher(int teacherId, Teacher2UpdateDto teacherDto) {

        Teacher2 existingTeacher = teacherRepository.findById(teacherId).orElseThrow(() -> new NotFoundException("teacher with id : " + teacherId + " not found"));

        boolean canI = adminHelper.canIUpdateTeacher(existingTeacher);

        if (!canI) {
            throw new UnAuthorizedException("Invalid teacher id : " + teacherId);
        }

        adminHelper.updateBasicDetailOfTeacher(existingTeacher, teacherDto);
        adminHelper.updateCredentialSubjectAndCourseOfTeacher(existingTeacher, teacherDto);

        return teacherRepository.save(existingTeacher);
    }

    @Transactional
    public void removeTeacherFromClass(int teacherId, int classId) {
        TuitionClass tc = getTuitionClassById(classId);
        Teacher2 teacher = adminHelper.getOwnTeacherById(teacherId);
        Tuition2 tuition = tc.getTuition();

        // Defensive null checks
        if (tc.getTeachers() != null) {
            tc.getTeachers().removeIf(t -> Objects.equals(t.getTeacherId(), teacherId));
        }

        if (teacher.getTuitionClasses() != null) {
            teacher.getTuitionClasses().removeIf(t -> Objects.equals(t.getTuitionClassId(), classId));
        }

        if (tuition.getTeacherIds() != null) {
            tuition.getTeacherIds().removeIf(tId -> Objects.equals(tId, teacherId));
        }

//        tuitionClassRepository.save(tc); // tution classes field has cascade all
        tuitionRepository.save(tc.getTuition());
        teacherRepository.save(teacher);
        subscriptionGuardService.onMemberRemoved(tuition.getTuitionAdmin());


    }

    @Transactional

    public Teacher2 addTeacherToTuitionClass(String teacherEmail, int tuitionClassId) {

        TuitionClass tuitionClass = adminHelper.getOwnTuitionClassById(tuitionClassId);
        Teacher2 teacher = adminHelper.getAnyTeacherByEmail(teacherEmail);
        Tuition2 tuition = tuitionClass.getTuition();

        // Ensure initialized
        if (tuitionClass.getTeachers() == null) {
            tuitionClass.setTeachers(new ArrayList<>());
        }
        if (tuition.getTeacherIds() == null) {
            tuition.setTeacherIds(new ArrayList<>());
        }
        if (teacher.getTuitionClasses() == null) {
            teacher.setTuitionClasses(new ArrayList<>());
        }

        // Check duplicate
        if (tuitionClass.getTeachers().contains(teacher)) {
            throw new AlreadyExists("Teacher already exists in this class");
        }

        // Add both sides
        tuitionClass.getTeachers().add(teacher);
        tuition.getTeacherIds().add(teacher.getTeacherId());
        teacher.getTuitionClasses().add(tuitionClass);

//        tuitionClassRepository.save(tuitionClass);
        tuitionRepository.save(tuition);
        return teacherRepository.save(teacher);
    }

    // --------- Subject operations ----------
    @Transactional
    public Subject2 createSubject(Subject2Dto subject2Dto) {
        TuitionClass tc = getTuitionClassById(subject2Dto.getTuitionClassId());
        tc.getSubjects().stream().filter(s -> s.getSubjectName().equals(subject2Dto.getSubjectName())).findAny().ifPresent(s -> {
            throw new AlreadyExists("Subject already exists with same name : " + s.getSubjectName());
        });

        Subject2 subject = new Subject2();
        subject.setSubjectName(subject2Dto.getSubjectName());
        subject.setTuitionClass(tc);

        // resources setup is remaining

        // add to class
        tc.getSubjects().add(subject);

        TuitionClass save = tuitionClassRepository.save(tc);

        Subject2 sub = save.getSubjects().stream().filter(s -> s.getSubjectName().equals(subject2Dto.getSubjectName())).findAny().get();

        return sub;
    }


    public List<Subject2> getAllSubjectsOfTuition(int tuitionId) {
        Tuition2 tuition = currentUser.getMyTuition(tuitionId);
        if (tuition == null) throw new UnAuthorizedException("Invalid tuition id");

        return subjectRepository.findAllByTuitionClassIn(tuition.getTuitionClasses());
    }


    public Subject2 getSubjectById(int subjectId) {

        String adminEmail = currentUser.getLoggedInUserEmail();

        Admin2 admin = adminRepository.findByAdminEmail(adminEmail).orElseThrow(() -> new NotFoundException("Admin Not found"));

        Subject2 subject = subjectRepository.findById(subjectId).orElseThrow(() -> new NotFoundException("Subject not found"));

        if (!subject.getTuitionClass().getTuition().getTuitionAdmin().equals(admin)) {
            throw new UnAuthorizedException("Invalid subject id : " + subjectId);
        }

        return subject;
    }


    @Transactional
    public Subject2 updateSubject(int subjectId, Subject2UpdateDto incoming) {
        Subject2 s = getSubjectById(subjectId);
        TuitionClass tc = getTuitionClassById(incoming.getTuitionClassId());

        s.setSubjectName(incoming.getSubjectName());
        s.setTuitionClass(tc);

        return subjectRepository.save(s);
    }

    @Transactional
    public void deleteSubject(int subjectId) {
        Subject2 subject = getSubjectById(subjectId);
        TuitionClass tc = subject.getTuitionClass();

        if (tc != null) {
            // Remove subject from the tuition class
            tc.getSubjects().removeIf(sub -> sub.getSubjectId() == subjectId);
            subject.setTuitionClass(null);
            tuitionClassRepository.save(tc);
        }

        // Unlink from students
        List<Student2> students = studentRepository.findAllBySubjectsContaining(subject);
        for (Student2 student : students) {
            student.getSubjects().remove(subject);
            studentRepository.save(student);
        }

        // Unlink from teachers
        List<Teacher2> teachers = teacherRepository.findAllBySubjectsContaining(subject);
        for (Teacher2 teacher : teachers) {
            teacher.getSubjects().remove(subject);
            teacherRepository.save(teacher);
        }

        // Now delete the subject
        subjectRepository.delete(subject);
    }


    // --------- Course operations ----------

    public List<Course2> getAllCoursesOfTuition(@Positive int tuitionId) {
        Tuition2 tuition = getTuitionById(tuitionId);
        return courseRepository.findAllByTuition(tuition);
    }


    @Transactional
    public Course2 createCourse(int tuitionId, Course2Dto courseDto) {
        Tuition2 tuition = getTuitionById(tuitionId);

        Course2 course = mapObjects.mapCourse(courseDto);

        // set relationship both sides
        course.setTuition(tuition);
        tuition.getCourses().add(course);

        // resources
        /// //////////// continue from here............
        if (courseDto.getResources() != null && !courseDto.getResources().isEmpty()) {
            List<MultipartFile> files = courseDto.getResources();
            System.out.println("files : " + files);
        }

        tuitionRepository.save(tuition);

        return course;
    }

    public Course2 getCourseById(int courseId) {
        Course2 course = courseRepository.findById(courseId).orElseThrow(() -> new NotFoundException("Course not found"));

        if (!currentUser.isMyTuition(course.getTuition().getTuitionId())) {
            throw new UnAuthorizedException("Invalid Course Id");
        }

        return course;
    }

    @Transactional
    public Course2 updateCourse(int courseId, Course2UpdateDto incoming) {
        Course2 c = getCourseById(courseId);
        c.setCourseName(incoming.getCourseName());
        c.setCourseDuration(incoming.getCourseDuration());
        c.setSubjectIds(incoming.getSubjectIds());

        return courseRepository.save(c);
    }

    @Transactional
    public void deleteCourse(int courseId) {
        Course2 c = getCourseById(courseId);
        // cleanup relations if needed
        courseRepository.delete(c);
    }

    // --------- Resource operations ----------



    /* ---------------------------
       HELPER Methods (existing + new)
       --------------------------- */

    private Admin2 getAdmin(String adminEmail) {
        return adminRepository.findByAdminEmail(adminEmail).orElseThrow(() -> new NotFoundException("Admin not found"));
    }


    
    public AllCount getAllCount(int adminId) {

        Admin2 admin = adminRepository.findById(adminId).orElseThrow(() -> new NotFoundException("Admin not found"));

        List<Tuition2> allTuition = admin.getAdminTuition();
        int totalTuition = allTuition.size();

        int totalTuitionClasses = allTuition.stream().mapToInt(t -> t.getTuitionClasses().size()).sum();

        int totalTeachers = (int) allTuition.stream().flatMap(t -> t.getTeacherIds().stream()).distinct().count();

        int totalStudents = (int) allTuition.stream().flatMap(t -> t.getStudentIds().stream()).distinct().count();

        return AllCount.builder().totalTuition(totalTuition).totalTuitionClasses(totalTuitionClasses).totalTeachers(totalTeachers).totalStudents(totalStudents).build();
    }


    public Admin2 myProfile() {
        return currentUser.getCurrentAdmin();
    }

    @Transactional
    public Admin2 updateProfile(Admin2Dto request) {
        Admin2 admin = currentUser.getCurrentAdmin();

        String newEmail = request.getAdminEmail();

        if(!admin.getAdminEmail().equals(newEmail) ){
            Credentials2 cred = credentialsRepository.findByEmail(newEmail).orElse(null);
            if(cred != null) {
                throw new AlreadyExists("Email already exists");
            }
        }

        Credentials2 credEntity = admin.getAdminCredential();
        credEntity.setEmail(newEmail);
        credentialsRepository.save(credEntity);

        admin.setAdminName(request.getAdminName());
        admin.setAdminPhoneNumber(request.getAdminPhoneNumber());
        admin.setAdminAddress(request.getAdminAddress());
        admin.setAdminEmail(newEmail);

        return adminRepository.save(admin);
    }

    @Transactional
    public Tuition2 createTuition(TuitionRequestDto dto) {

        Admin2 admin = currentUser.getCurrentAdmin();

        // 1. Lock counter row
        TuitionCodeCounter counter = counterRepository.lockAndGet();

        int newCode = counter.getNextCode();

        // 2. Increment for next use
        counter.setNextCode(newCode + 1);

        // 3. Create tuition
        Tuition2 tuition = new Tuition2();
        tuition.setTuitionCode(newCode);

        tuition.setTuitionName(dto.getTuitionName());
        tuition.setTuitionEmail(dto.getTuitionEmail());
        tuition.setTuitionPhoneNumber(dto.getTuitionPhoneNumber());
        tuition.setTuitionAddress(dto.getTuitionAddress());
        tuition.setBranch(dto.getBranch());
        tuition.setTuitionAdmin(admin);

        // 4. Save tuition
        tuitionRepository.save(tuition);

        // 5. Persist updated counter
        counterRepository.save(counter);

        return tuition;
    }


    public List<JoinRequestTuition> getTuitionJoinRequests(int tuitionId) {
        Object user = currentUser.getLoggedInUser();

        if (user instanceof Admin2 admin) {
            Optional<Tuition2> optTuition = admin.getAdminTuition().stream().filter(t -> t.getTuitionId() == tuitionId).findAny();
            if (optTuition.isEmpty()) {
                throw new NotFoundException("Tuition not Found");
            } else {

                Tuition2 tuition = optTuition.get();
                int tuitionCode = tuition.getTuitionCode();

                return requestTuitionRepository.findByTuitionCode(tuitionCode);

            }
        } else {
            throw new UnAuthorizedException("Access Denied");
        }
    }

    @Transactional
    public JoinRequestTuition processRequest(ProcessJoinRequest request) {

        JoinRequestTuition req = requestTuitionRepository.findById(request.getRequestId()).orElseThrow(() -> new NotFoundException("Request not found"));
        int tuitionCode = req.getTuitionCode();

        Tuition2 tuition = tuitionRepository.findByTuitionCode(tuitionCode).orElseThrow(() -> new NotFoundException("Tuition not found"));

        Object user = currentUser.getLoggedInUser();

        if (user instanceof Admin2 admin) {

            if (admin.getAdminTuition().stream().filter(t -> t.getTuitionId() == tuition.getTuitionId()).findAny().isEmpty()) {
                throw new UnAuthorizedException("Invalid tuition code");
            }

            if (request.isApprove()) {
                req.setStatus(JoinRequestTuition.JoinStatus.APPROVED);
            } else {
                req.setStatus(JoinRequestTuition.JoinStatus.REGRET);
            }

            return requestTuitionRepository.save(req);
        } else {
            throw new UnAuthorizedException("Access Denied");
        }


    }


    public StudentDetail getStudentDetail(int requestId) {

        JoinRequestTuition joinRequestTuition = requestTuitionRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Request not found"));

        if (joinRequestTuition.isTeacher()) {
            return null;
        }

        Student2 student = studentRepository.findById(joinRequestTuition.getUserId()).orElseThrow(() -> new NotFoundException("Student not found"));

        StudentDetail detail = new StudentDetail();
        detail.setStudentId(student.getStudentId());
        detail.setStudentName(student.getStudentName());
        detail.setPhoneNumber(student.getStudentPhoneNumber());
        detail.setDateOfBirth(student.getBirthDate());
        detail.setTotalClassesEnrolled(student.getTuitionClasses().size());

        return detail;

    }

    public TeacherDetail getTeacherDetail(int requestId) {

        JoinRequestTuition joinRequestTuition = requestTuitionRepository.findById(requestId).orElseThrow(() -> new NotFoundException("Request not found"));

        if (!joinRequestTuition.isTeacher()) {
            return null;
        }

        Teacher2 teacher = teacherRepository.findById(joinRequestTuition.getUserId()).orElseThrow(() -> new NotFoundException("Teacher not found"));

        TeacherDetail detail = new TeacherDetail();
        detail.setTeacherId(teacher.getTeacherId());
        detail.setTeacherName(teacher.getTeacherName());
        detail.setPhoneNumber(teacher.getTeacherPhoneNumber());
        detail.setExperience(teacher.getExperience());
        detail.setSubjects(teacher.getKnownSubjects());

        return detail;

    }

    public List<Student2> getApprovedStudents(int tuitionId) {

        Admin2 admin = currentUser.getCurrentAdmin();

        Optional<Tuition2> optTuition = admin.getAdminTuition().stream().filter(t -> t.getTuitionId() == tuitionId).findAny();

        if (optTuition.isEmpty()) {
            throw new UnAuthorizedException("Invalid tuition id");
        }

        Tuition2 tuition = optTuition.get();

        List<JoinRequestTuition> approvedRequests = requestTuitionRepository.findByTuitionCodeAndStatusAndIsTeacher(tuition.getTuitionCode(), JoinRequestTuition.JoinStatus.APPROVED, false);

        return studentRepository.findAllById(approvedRequests.stream().map(JoinRequestTuition::getUserId).toList());

    }


    public List<Teacher2> getApprovedTeachers(int tuitionId) {

        Admin2 admin = currentUser.getCurrentAdmin();

        Optional<Tuition2> optTuition = admin.getAdminTuition().stream().filter(t -> t.getTuitionId() == tuitionId).findAny();

        if (optTuition.isEmpty()) {
            throw new UnAuthorizedException("Invalid tuition id");
        }


        Tuition2 tuition = optTuition.get();

        List<JoinRequestTuition> approvedRequests = requestTuitionRepository.findByTuitionCodeAndStatusAndIsTeacher(tuition.getTuitionCode(), JoinRequestTuition.JoinStatus.APPROVED, true);

        return teacherRepository.findAllById(approvedRequests.stream().map(JoinRequestTuition::getUserId).toList());

    }

    
    @Transactional
    public Teacher2 enrollTeacher(EnrollTeacherRequest request) {

        Admin2 admin = currentUser.getCurrentAdmin();

        // 🔒 BLOCK if expired or limit reached
        subscriptionGuardService.assertCanAddMember(admin);

        int teacherId = request.getTeacherId();
        int tuitionId = request.getTuitionId();
        List<Integer> subjectIds = request.getSubjectIds();
        List<Integer> courseIds = request.getCourseIds();


        Teacher2 teacher = teacherRepository.findById(teacherId).orElseThrow(() -> new NotFoundException("Teacher not found"));

        Optional<Tuition2> optTuition = admin.getAdminTuition().stream().filter(t -> t.getTuitionId() == tuitionId).findAny();

        if (optTuition.isEmpty()) {
            throw new UnAuthorizedException("Invalid tuition id");
        }

        Tuition2 tuition = optTuition.get();

        if (tuition.getTeacherIds().contains(teacherId)) {
            throw new AlreadyExists("Teacher already enrolled in this tuition");
        }


        //add subjects
        if (!subjectIds.isEmpty()) {
            List<Subject2> subjects = subjectRepository.findAllById(subjectIds);

            if (subjects.size() != subjectIds.size())
                throw new NotFoundException("Some of the subjects does not exists");

            // Ensure all subjects belong to this admin
            if (subjects.stream().anyMatch(s -> s.getTuitionClass().getTuition().getTuitionId() != tuitionId)) {
                throw new UnAuthorizedException("Invalid subjects provided");
            }

            teacher.getSubjects().addAll(subjects);

            // Add tuition classes linked to these subjects
            List<TuitionClass> tuitionClasses = subjects.stream()
                    .map(Subject2::getTuitionClass)
                    .distinct()
                    .toList();
            teacher.getTuitionClasses().addAll(tuitionClasses);
            tuitionClasses.forEach(tc -> tc.getTeachers().add(teacher));
        }

        // Add courses
        if (!courseIds.isEmpty()) {
            List<Course2> courses = courseRepository.findAllById(courseIds);
            if (courses.size() != courseIds.size()) {
                throw new InvalidOptionException("Invalid Course ids provided");
            }
            // Ensure all courses belong to this tuition
            List<Course2> validCourses = courses.stream()
                    .filter(c -> c.getTuition().getTuitionId() == tuitionId)
                    .toList();
            if (validCourses.size() != courses.size()) {
                throw new UnAuthorizedException("Invalid courses provided");
            }
            teacher.getCourses().addAll(validCourses);
        }

        teacher.getClassJoined().add(new ClassJoined(tuition.getTuitionId(), LocalDateTime.now()));

        // ✅ Save and link teacher
        Teacher2 response = teacherRepository.save(teacher);
        tuition.getTeacherIds().add(response.getTeacherId());
        tuitionRepository.save(tuition);

        subscriptionGuardService.onMemberAdded(admin);

        requestTuitionRepository.deleteByTuitionCodeAndStatusAndIsTeacherAndUserId(tuition.getTuitionCode(), JoinRequestTuition.JoinStatus.APPROVED, true, teacherId);

        return response;
    }

    @Transactional
    public Student2 enrollStudent(@Valid EnrollStudentRequest request) {

        Admin2 admin = currentUser.getCurrentAdmin();

        // 🔒 SUBSCRIPTION CHECK
        subscriptionGuardService.assertCanAddMember(admin);

        int studentId = request.getStudentId();
        int tuitionClassId = request.getTuitionClassId();
        List<Integer> subjectIds = request.getSubjectIds();
        List<Integer> courseIds = request.getCourseIds();

        Student2 student = studentRepository.findById(studentId).orElseThrow(() -> new NotFoundException("Student not found"));

        List<TuitionClass> tuitionClasses = admin.getAdminTuition().stream().flatMap(t -> t.getTuitionClasses().stream()).toList();

        Optional<TuitionClass> optTuitionClass = tuitionClasses.stream().filter(tc -> tc.getTuitionClassId() == tuitionClassId).findAny();

        if (optTuitionClass.isEmpty()) {
            throw new UnAuthorizedException("Invalid tuition class");
        }

        TuitionClass tuitionClass = optTuitionClass.get();
        Tuition2 tuition = tuitionClass.getTuition();

        if (tuition.getStudentIds().contains(studentId)) {
            throw new AlreadyExists("Student already enrolled in this tuition");
        }


        List<Subject2> subjects = tuitionClass.getSubjects().stream().filter(s -> subjectIds.contains(s.getSubjectId())).toList();
        if (!subjects.isEmpty()) {

            if (subjects.size() != subjectIds.size()) {
                throw new NotFoundException("Some of the subjects does not exists");
            }

            student.getSubjects().addAll(subjects);

            student.getTuitionClasses().add(tuitionClass);

        }

        List<Course2> courses = courseRepository.findAllById(courseIds);

        if (!courses.isEmpty()) {
            List<Course2> validCourses = courses.stream().filter(c -> c.getTuition().getTuitionId() == tuitionClass.getTuition().getTuitionId()).toList();

            if (validCourses.size() != courseIds.size())
                throw new InvalidOptionException("Some of the courses does not exists or Invalid");

            student.getCourses().addAll(validCourses);
        }

        student.getClassJoined().add(new ClassJoined(tuition.getTuitionId(), LocalDateTime.now()));

        // Save student and update tuition
        Student2 response = studentRepository.save(student);
        tuitionClass.getStudents().add(student);
        tuition.getStudentIds().add(response.getStudentId());
        tuitionRepository.save(tuition);

        subscriptionGuardService.onMemberAdded(admin);
        requestTuitionRepository.deleteByTuitionCodeAndStatusAndIsTeacherAndUserId(tuition.getTuitionCode(), JoinRequestTuition.JoinStatus.APPROVED, false, studentId);

        return response;

    }

    @Transactional
    public Resource2 uploadResource(MultipartFile file, int tuitionId, Integer courseId, Integer subjectId, Integer folderId
    ) throws Exception {

        // ----- course / subject exclusivity -----
        if ((courseId == null && subjectId == null) ||
                (courseId != null && subjectId != null)) {
            throw new IllegalArgumentException("Provide either courseId or subjectId");
        }

        // ----- validate tuition -----
        Admin2 admin = currentUser.getCurrentAdmin();

        // 🔒 SUBSCRIPTION STORAGE CHECK
        subscriptionGuardService.assertCanUpload(admin, file.getSize());


        Tuition2 tuition = admin.getAdminTuition().stream()
                .filter(t -> Objects.equals(t.getTuitionId(), tuitionId))
                .findAny()
                .orElseThrow(() -> new NotFoundException("Tuition not found"));

        // ----- validate scope entity -----
        if (courseId != null) {
            Course2 course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new NotFoundException("Course not found"));

            if (!Objects.equals(course.getTuition().getTuitionId(), tuitionId)) {
                throw new UnAuthorizedException("Invalid course id");
            }
        }

        if (subjectId != null) {
            Subject2 subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new NotFoundException("Subject not found"));

            if (!Objects.equals(
                    subject.getTuitionClass().getTuition().getTuitionId(),
                    tuitionId)) {
                throw new UnAuthorizedException("Invalid subject id");
            }
        }

        // ----- validate folder -----
        ResourceFolder2 folder = null;
        if (folderId != null) {
            folder = folderRepository2.findById(folderId)
                    .orElseThrow(() -> new NotFoundException("Folder not found"));

            if (!Objects.equals(folder.getTuitionId(), tuitionId)) {
                throw new IllegalArgumentException("Folder belongs to another tuition");
            }

            if (courseId != null) {
                if (!Objects.equals(folder.getCourseId(), courseId)) {
                    throw new IllegalArgumentException("Folder does not belong to this course");
                }
            } else { // subjectId != null
                if (!Objects.equals(folder.getSubjectId(), subjectId)) {
                    throw new IllegalArgumentException("Folder does not belong to this subject");
                }
            }

        }

        // ----- generate MinIO object key -----
        String objectKey =
                "tuition-" + tuitionId + "/" +
                        UUID.randomUUID() + "-" + file.getOriginalFilename();

        // ----- upload -----
        try (InputStream is = file.getInputStream()) {
            minioService.uploadToMinio(
                    is,
                    file.getSize(),
                    file.getContentType(),
                    objectKey
            );
        }

        // ----- persist metadata -----
        Resource2 resource = new Resource2();
        resource.setCredentialId(admin.getAdminCredential().getUserId());
        resource.setName(file.getOriginalFilename());
        resource.setType(file.getContentType());
        resource.setSize(file.getSize());
        resource.setFileName(objectKey);
        resource.setUploadedAt(LocalDateTime.now());
        resource.setTuitionId(tuitionId);
        resource.setCourseId(courseId);
        resource.setSubjectId(subjectId);
        resource.setFolder(folder);

        Resource2 saved = resourceRepository2.save(resource);

        // ✅ INCREMENT STORAGE USAGE
        subscriptionGuardService.onFileUploaded(admin, file.getSize());

        return saved;

    }

    @Transactional
    public ResourceFolder2 createFolder2(ResourceFolderRequest req) {

        Admin2 admin = currentUser.getCurrentAdmin();

        Tuition2 tuition = admin.getAdminTuition().stream()
                .filter(t -> Objects.equals(t.getTuitionId(), req.getTuitionId()))
                .findAny()
                .orElseThrow(() -> new NotFoundException("Tuition not found"));

        // ----- exclusivity -----
        if ((req.getCourseId() == null && req.getSubjectId() == null) ||
                (req.getCourseId() != null && req.getSubjectId() != null)) {
            throw new IllegalArgumentException(
                    "Provide either courseId or subjectId"
            );
        }

        if (req.getCourseId() != null) {
            Course2 course = courseRepository.findById(req.getCourseId())
                    .orElseThrow(() -> new NotFoundException("Course not found"));

            if (!Objects.equals(course.getTuition().getTuitionId(), tuition.getTuitionId())) {
                throw new UnAuthorizedException("Invalid course");
            }
        }

        if (req.getSubjectId() != null) {
            Subject2 subject = subjectRepository.findById(req.getSubjectId())
                    .orElseThrow(() -> new NotFoundException("Subject not found"));

            if (!Objects.equals(
                    subject.getTuitionClass().getTuition().getTuitionId(),
                    tuition.getTuitionId())) {
                throw new UnAuthorizedException("Invalid subject");
            }
        }

        ResourceFolder2 parent = null;
        if (req.getParentFolderId() != null) {
            parent = folderRepository2.findById(req.getParentFolderId())
                    .orElseThrow(() -> new NotFoundException("Parent folder not found"));

            if (!Objects.equals(parent.getTuitionId(), req.getTuitionId())) {
                throw new IllegalArgumentException("Parent folder belongs to another tuition");
            }


            // Inherit scope from parent
            req.setCourseId(parent.getCourseId());
            req.setSubjectId(parent.getSubjectId());
        }

        // ----- first save -----
        ResourceFolder2 folder = new ResourceFolder2();
        folder.setName(req.getName());
        folder.setTuitionId(req.getTuitionId());
        folder.setCourseId(req.getCourseId());
        folder.setSubjectId(req.getSubjectId());
        folder.setParentFolder(parent);

        folder = folderRepository2.save(folder);

        // ----- materialized path -----
        folder.setPath(
                parent == null
                        ? "/" + folder.getFolderId()
                        : parent.getPath() + "/" + folder.getFolderId()
        );

        return folderRepository2.save(folder);
    }


    public FolderBrowseResponse browse(
            int tuitionId,
            Integer courseId,
            Integer subjectId,
            Integer folderId
    ) {

        if ((courseId == null && subjectId == null) ||
                (courseId != null && subjectId != null)) {
            throw new IllegalArgumentException("Provide either courseId or subjectId");
        }

        Admin2 admin = currentUser.getCurrentAdmin();

        admin.getAdminTuition().stream()
                .filter(t -> Objects.equals(t.getTuitionId(), tuitionId))
                .findAny()
                .orElseThrow(() -> new NotFoundException("Tuition not found"));

        if (courseId != null) {
            Course2 course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new NotFoundException("Course not found"));

            if (!Objects.equals(course.getTuition().getTuitionId(), tuitionId)) {
                throw new UnAuthorizedException("Invalid course id");
            }
        }

        if (subjectId != null) {
            Subject2 subject = subjectRepository.findById(subjectId)
                    .orElseThrow(() -> new NotFoundException("Subject not found"));

            if (!Objects.equals(
                    subject.getTuitionClass().getTuition().getTuitionId(),
                    tuitionId)) {
                throw new UnAuthorizedException("Invalid subject id");
            }
        }

        List<ResourceFolderDto2> folderDtos;
        List<ResourceResponse> resourceDtos;

        if (folderId == null) {
            if (courseId != null) {
                folderDtos = folderRepository2
                        .findByTuitionIdAndParentFolderIsNullAndCourseId(tuitionId, courseId)
                        .stream().map(this::toFolderDto).toList();

                resourceDtos = resourceRepository2
                        .findByTuitionIdAndCourseIdAndFolderIsNull(tuitionId, courseId)
                        .stream().map(mapObjects::mapResourceResponse2).toList();
            } else {
                folderDtos = folderRepository2
                        .findByTuitionIdAndParentFolderIsNullAndSubjectId(tuitionId, subjectId)
                        .stream().map(this::toFolderDto).toList();

                resourceDtos = resourceRepository2
                        .findByTuitionIdAndSubjectIdAndFolderIsNull(tuitionId, subjectId)
                        .stream().map(mapObjects::mapResourceResponse2).toList();
            }
        } else {
            folderDtos = folderRepository2
                    .findByParentFolder_FolderId(folderId)
                    .stream().map(this::toFolderDto).toList();

            resourceDtos = resourceRepository2
                    .findByFolder_FolderId(folderId)
                    .stream().map(mapObjects::mapResourceResponse2).toList();
        }

        return new FolderBrowseResponse(folderDtos, resourceDtos);
    }


    @Transactional
    public void deleteResource(int resourceId) {

        Resource2 resource = resourceRepository2.findById(resourceId)
                .orElseThrow(() -> new NotFoundException("Resource not found"));

        int tuitionId = resource.getTuitionId();

        Admin2 admin = currentUser.getCurrentAdmin();

        if (admin.getAdminTuition().stream().noneMatch(t -> t.getTuitionId() == tuitionId)) {
            throw new UnAuthorizedException("You can't delete this Resource");
        }

        String objectKey = resource.getFileName();

        // 1️⃣ delete from MinIO first
        try {
            minioService.deleteObject(objectKey);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file from storage", e);
        }

        // 2️⃣ delete metadata
        resourceRepository2.delete(resource);
        subscriptionGuardService.onFileDeleted(admin, resource.getSize());

    }


    @Transactional
    public void deleteFolder(int folderId) {

        // ---------- root folder ----------
        ResourceFolder2 root = folderRepository2.findById(folderId)
                .orElseThrow(() -> new NotFoundException("Folder not found"));

        Admin2 admin = currentUser.getCurrentAdmin();

        if (admin.getAdminTuition().stream().noneMatch(t -> t.getTuitionId() == root.getTuitionId())) {
            throw new UnAuthorizedException("Invalid folder");
        }

        // ---------- subtree folders ----------
        List<ResourceFolder2> folders =
                folderRepository2.findByPathStartingWith(root.getPath());

        // ---------- all resources in subtree ----------
        List<Resource2> resources =
                resourceRepository2.findByFolderFolderIdIn(folders.stream().map(ResourceFolder2::getFolderId).toList());

        // ---------- delete all files from MinIO ----------
        for (Resource2 r : resources) {
            try {
                if (r.getCredentialId() != admin.getAdminCredential().getUserId()) {
                    throw new UnAuthorizedException("Invalid resource");
                }
                minioService.deleteObject(r.getFileName());
            } catch (Exception e) {
                throw new RuntimeException(
                        "Failed to delete file from storage: " + r.getFileName(), e
                );
            }
        }

        // ---------- delete DB rows ----------
        resourceRepository2.deleteAllById(resources.stream().map(Resource2::getResourceId).toList());
        folderRepository2.deleteAllById(folders.stream().map(ResourceFolder2::getFolderId).toList());
    }

    @Transactional
    public void renameFolder(int folderId, String newName) {

        ResourceFolder2 folder = folderRepository2.findById(folderId)
                .orElseThrow(() -> new NotFoundException("Folder not found"));

        Admin2 admin = currentUser.getCurrentAdmin();

        if (admin.getAdminTuition().stream().noneMatch(t -> t.getTuitionId() == folder.getTuitionId())) {
            throw new UnAuthorizedException("Invalid Folder");
        }

        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Folder name cannot be empty");
        }

        folder.setName(newName.trim());
        folderRepository2.save(folder);
    }


    @Transactional
    public void renameResource(int resourceId, String newName) {

        Resource2 resource = resourceRepository2.findById(resourceId)
                .orElseThrow(() -> new NotFoundException("Resource not found"));

        Admin2 admin = currentUser.getCurrentAdmin();

        if (admin.getAdminTuition().stream().noneMatch(t -> t.getTuitionId() == resource.getTuitionId())) {
            throw new UnAuthorizedException("You can't rename this Resource");
        }

        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("File name cannot be empty");
        }

        resource.setName(newName.trim());
        resourceRepository2.save(resource);
    }

    public String getPresignedUrl(int resourceId) {

        Resource2 resource = resourceRepository2.findById(resourceId)
                .orElseThrow(() -> new NotFoundException("Resource not found"));

        Admin2 admin = currentUser.getCurrentAdmin();

        if (admin.getAdminTuition().stream().noneMatch(t -> t.getTuitionId() == resource.getTuitionId())) {
            throw new UnAuthorizedException("You can't access this resource");
        }

        return minioService.generatePresignedGetUrl(
                resource.getFileName(),
                60 * 60
        );
    }


    // ---------- mappers ----------
    private ResourceFolderDto2 toFolderDto(ResourceFolder2 f) {
        return new ResourceFolderDto2(
                f.getFolderId(),
                f.getName()
        );
    }



    @Transactional
    public void deleteTuitionById(int tuitionId) {

        Admin2 admin = currentUser.getCurrentAdmin();

        Tuition2 tuition = tuitionRepository.findFullGraphById(tuitionId)
                .orElseThrow(() -> new NotFoundException("Tuition not found"));

        if (!admin.getAdminTuition().contains(tuition)) {
            return;
        }

        // ---------- 1. Collect resource files (no entity hydration) ----------
        List<Object[]> files = resourceRepository2.findFileNamesAndSizesByTuitionId(tuitionId);

        List<String> fileNames = new ArrayList<>(files.size());
        long totalSize = 0;

        for (Object[] row : files) {
            fileNames.add((String) row[0]);
            totalSize += ((Number) row[1]).longValue();
        }

        // ---------- 2. Break relations (O(N), no removeIf) ----------

        for (TuitionClass tc : tuition.getTuitionClasses()) {

            tc.getStudents().forEach(s -> {
                s.getTuitionClasses().remove(tc);
                s.getSubjects().removeIf(sb -> sb.getTuitionClass() == tc);
            });
            tc.getStudents().clear();

            tc.getTeachers().forEach(t -> {
                t.getTuitionClasses().remove(tc);
                t.getSubjects().removeIf(sb -> sb.getTuitionClass() == tc);
            });
            tc.getTeachers().clear();

            tc.getSubjects().forEach(sub -> {
                sub.getStudents().clear();
                sub.getTeachers().clear();
            });
        }

        for (Course2 c : tuition.getCourses()) {
            c.getStudents().forEach(s -> s.getCourses().remove(c));
            c.getTeachers().forEach(t -> t.getCourses().remove(c));
            c.getStudents().clear();
            c.getTeachers().clear();
        }

        admin.getAdminTuition().remove(tuition);
        tuition.setTuitionAdmin(null);

        // ---------- 3. Bulk SQL deletes (no entity loading) ----------

        messageRecipientRepository.deleteAllByTuitionId(tuitionId);
        broadcastMessageRepository.deleteAllByTuitionId(tuitionId);
        chatReadRepository.deleteAllByTuitionId(tuitionId);
        announcementReadRepository.deleteAllByTuitionId(tuitionId);
        folderRepository2.deleteAllByTuitionId(tuitionId);
        resourceRepository2.deleteAllByTuitionId(tuitionId);
        classJoinedRepository.deleteAllByTuitionId(tuition.getTuitionId());

        // ---------- 4. Flush & clear to avoid memory pressure ----------
        entityManager.flush();
        entityManager.clear();

        // ---------- 5. Delete tuition itself ----------
        tuitionRepository.deleteById(tuitionId);

        // ---------- 6. Delete MinIO files AFTER commit ----------
        long finalTotalSize = totalSize;
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        executor.submit(() -> {
                            for (String f : fileNames) {
                                try {
                                    minioService.deleteObject(f);
                                } catch (Exception e) {
                                    // log and continue
                                }
                            }
                            subscriptionGuardService.onFileDeleted(admin, finalTotalSize);
                        });
                    }
                }
        );
    }



//    @Transactional
//    public void deleteTuitionById(int tuitionId) {
//
//        Admin2 admin = currentUser.getCurrentAdmin();
//
//        Optional<Tuition2> optTuition = admin.getAdminTuition().stream().filter(t -> t.getTuitionId() == tuitionId).findAny();
//
//        if (optTuition.isEmpty()) {
//            return;
//        }
//
//        Tuition2 tuition = optTuition.get();
//
//        // 1. Break Student <-> TuitionClass links
//        for (TuitionClass tc : tuition.getTuitionClasses()) {
//            for (Student2 s : tc.getStudents()) {
//                s.getTuitionClasses().removeIf(tcl -> tcl.getTuitionClassId() == tc.getTuitionClassId());
//                s.getSubjects().removeIf(sb-> sb.getTuitionClass().getTuitionClassId() == tc.getTuitionClassId());
//                s.getCourses().removeIf(c-> c.getTuition().getTuitionId() == tc.getTuition().getTuitionId());
//            }
//            tc.getStudents().clear();
//
//            for (Teacher2 t : tc.getTeachers()) {
//                t.getTuitionClasses().removeIf(tcl -> tcl.getTuitionClassId() == tc.getTuitionClassId());
//                t.getSubjects().removeIf(s-> s.getTuitionClass().getTuitionClassId() == tc.getTuitionClassId());
//                t.getCourses().removeIf(c-> c.getTuition().getTuitionId() == tc.getTuition().getTuitionId());
//
//            }
//            tc.getTeachers().clear();
//        }
//
//
//        for (Course2 c : tuition.getCourses()) {
//            for (Student2 s : c.getStudents()) {
//                s.getCourses().remove(c);
//            }
//            c.getStudents().clear();
//        }
//
//        for (Course2 c : tuition.getCourses()) {
//            for (Teacher2 t : c.getTeachers()) {
//                t.getCourses().remove(c);
//            }
//            c.getTeachers().clear();
//        }
//
//
//        for (TuitionClass tc : tuition.getTuitionClasses()) {
//            for (Subject2 sub : tc.getSubjects()) {
//                sub.getStudents().clear();
//                sub.getTeachers().clear();
//            }
//        }
//
//        List<Resource2> resources = resourceRepository2.findAllByTuitionId(tuitionId);
//
//        resources.stream().map(Resource2::getFileName)
//                .parallel()
//                .forEach(minioService::deleteObject);
//
//
//        resourceRepository2.deleteAllById(resources.stream().map(Resource2::getResourceId).toList());
//
//        long totalSize = 0;
//
//        for (Resource2 resource : resources) {
//            totalSize += resource.getSize();
//        }
//
//        subscriptionGuardService.onFileDeleted(admin, totalSize);
//
//        folderRepository2.deleteAllByTuitionId(tuitionId);
//
//        messageRecipientRepository.deleteByMessageTuitionId(tuitionId);
//
//        broadcastMessageRepository.deleteByTuitionId(tuitionId);
//
//        chatReadRepository.deleteByTuitionId(tuitionId);
//
//        announcementReadRepository.deleteByTuitionId(tuitionId);
//
//        admin.getAdminTuition().removeIf(t -> t.getTuitionId() == tuition.getTuitionId());
//        tuition.setTuitionAdmin(null);
//
//        // 2. Now delete tuition (cascade will delete EVERYTHING else)
//        tuitionRepository.delete(tuition);
//    }

    public List<SubscriptionPlan> getSubscriptionPlans() {
        List<SubscriptionPlan> plans = subscriptionPlanRepository.findAll();
        plans.sort(Comparator.comparingInt(SubscriptionPlan::getPricePerMonth));
        return plans;
    }

    public SubscriptionAdminResponse getCurrentSubscription() {
        int adminId = currentUser.getCurrentAdmin().getAdminId();

        return adminRepository
                .getSubscriptionByAdminId(adminId)
                .orElseThrow(() -> new RuntimeException("No subscription found for admin"));

    }


}