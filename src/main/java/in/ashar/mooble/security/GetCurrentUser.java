package in.ashar.mooble.security;

import in.ashar.mooble.entity.Admin2;
import in.ashar.mooble.entity.Student2;
import in.ashar.mooble.entity.Teacher2;
import in.ashar.mooble.entity.Tuition2;
import in.ashar.mooble.exception.NotFoundException;
import in.ashar.mooble.repository.Admin2Repository;
import in.ashar.mooble.repository.Student2Repository;
import in.ashar.mooble.repository.Teacher2Repository;
import in.ashar.mooble.repository.Tuition2Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetCurrentUser {

    public final Student2Repository studentRepository;
    public final Admin2Repository adminRepository;
    public final Tuition2Repository tuitionRepository;
    public final Teacher2Repository teacherRepository;


    public Object getLoggedInUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        Student2 student = studentRepository.findByStudentCredentialEmail(email).orElse(null);
        if(student == null){

            Teacher2 teacher = teacherRepository.findByTeacherCredentialEmail(email).orElse(null);
            if(teacher == null){

                return adminRepository.findByAdminEmail(email).orElseThrow(() -> new NotFoundException("User not found"));

            }else {
                return teacher;
            }

        }
        else{
            return student;
        }
    }

    public String getLoggedInUserEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getName(); // this is usually the email/username
    }


    public Admin2 getCurrentAdmin() {
        String adminEmail = getLoggedInUserEmail();
        return adminRepository.findByAdminEmail(adminEmail).orElseThrow(() -> new NotFoundException("Admin not found"));
    }

    public Teacher2 getCurrentTeacher() {
        String teacherEmail = getLoggedInUserEmail();
        return teacherRepository.findByTeacherCredentialEmail(teacherEmail).orElseThrow(() -> new NotFoundException("Teacher not found"));
    }

    public Student2 getCurrentStudent() {
        String studentEmail = getLoggedInUserEmail();
        return studentRepository.findByStudentCredentialEmail(studentEmail).orElseThrow(() -> new NotFoundException("Student not found"));
    }


    public boolean isMyStudent(int studentId){

        String adminEmail = getLoggedInUserEmail();
        Admin2 admin = adminRepository.findByAdminEmail(adminEmail).orElseThrow(() -> new NotFoundException("Admin not found"));

        List<Tuition2> tuitionList = admin.getAdminTuition();

        Optional<Integer> any = tuitionList.stream().flatMap(t -> t.getStudentIds().stream()).distinct().filter(id -> id == studentId).findAny();

        return any.isPresent();
    }

    public boolean isMyTuition(int tuitionId){

        String adminEmail = getLoggedInUserEmail();
        Admin2 admin = adminRepository.findByAdminEmail(adminEmail).orElseThrow(() -> new NotFoundException("Admin not found"));

        Tuition2 tuition = tuitionRepository.findById(tuitionId).orElseThrow(() -> new NotFoundException("Tuition not found"));

        return tuition.getTuitionAdmin().equals(admin);
    }

    public Tuition2 getMyTuition(int tuitionId){

        String adminEmail = getLoggedInUserEmail();
        Admin2 admin = adminRepository.findByAdminEmail(adminEmail).orElseThrow(() -> new NotFoundException("Admin not found"));

        Tuition2 tuition = tuitionRepository.findById(tuitionId).orElseThrow(() -> new NotFoundException("Tuition not found"));

        if(tuition.getTuitionAdmin().equals(admin)){
            return tuition;
        }

        return null;
    }

    public boolean isMyTuitionAndStudent(int tuitionId, int studentId){

        Tuition2 tuition = tuitionRepository.findById(tuitionId).orElseThrow(() -> new NotFoundException("Tuition not found"));

        String adminEmail = getLoggedInUserEmail();
        Admin2 admin = adminRepository.findByAdminEmail(adminEmail).orElseThrow(() -> new NotFoundException("Admin not found"));

        if(!tuition.getTuitionAdmin().equals(admin)){
            return false;
        }

        return tuition.getStudentIds().contains(studentId);
    }

    public List<Tuition2> getCurrentAdminTuition(){

        String adminEmail = getLoggedInUserEmail();
        Admin2 admin = adminRepository.findByAdminEmail(adminEmail).orElseThrow(() -> new NotFoundException("Admin not found"));

        return admin.getAdminTuition();

    }
}
