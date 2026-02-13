//package in.ashar.mooble.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import in.ashar.mooble.exception.NotFoundException;
//import in.ashar.mooble.exception.UnAuthorizedException;
//import in.ashar.mooble.security.GetCurrentUser;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//public class TuitionService {
//
//    @Autowired
//    private ObjectMapper objectMapper;
//    @Autowired
//    private GetCurrentUser getCurrentUser;
//
//    private final TuitionRepository tuitionRepository;
//    private final UserRepository userRepository;
//    private final TuitionUserRepository tuitionUserRepository;
//
//    public TuitionService(TuitionRepository tuitionRepository, UserRepository userRepository,TuitionUserRepository tuitionUserRepository){
//        this.tuitionRepository = tuitionRepository;
//        this.userRepository = userRepository;
//        this.tuitionUserRepository = tuitionUserRepository;
//    }
//
//    public List<Tuition> getAll() {
//        return tuitionRepository.findAll();
//    }
//
//    public Tuition getById(Long id) {
//        return tuitionRepository.findById(id).orElseThrow(()-> new NotFoundException("tuition not found"));
//    }
//
//    public Tuition create(TuitionDto tuitionDto) {
//
//        User admin = userRepository.findById(tuitionDto.getAdminId()).orElseThrow(() -> new NotFoundException("Admin not Found"));
//        String roleName = admin.getRole().name();
//
//        if(!roleName.equals("ADMIN")){
//            throw new UnAuthorizedException("Invalid Admin Id");
//        }
//
//        Tuition tuition = objectMapper.convertValue(tuitionDto, Tuition.class);
//        tuition.setAdmin(admin);
//
//        Tuition saved = tuitionRepository.save(tuition);
//
//        TuitionUser tuitionUser = new TuitionUser();
//        tuitionUser.setUser(admin);
//        tuitionUser.setTuition(saved);
//        tuitionUser.setJoinedOn(LocalDateTime.now());
//
//        tuitionUserRepository.save(tuitionUser);
//
//        return saved;
//    }
//
//    public Tuition update(Long id, TuitionDto tuitionDto) {
//        Tuition oldTuition = tuitionRepository.findById(id).orElseThrow(() -> new NotFoundException("Tuition not found"));
//        oldTuition.setName(tuitionDto.getName());
//        oldTuition.setEmail(tuitionDto.getEmail());
//        oldTuition.setAddress(tuitionDto.getAddress());
//        oldTuition.setContactNumber(tuitionDto.getContactNumber());
//        return tuitionRepository.save(oldTuition);
//    }
//
//    public void delete(Long id) {
//        tuitionRepository.deleteById(id);
//    }
//
//    public List<Tuition> getAllTuitionOfAdmin() {
//
//        String adminEmail = getCurrentUser.getLoggedInUserEmail();
//
//        return tuitionRepository.getAllTuitionOfAdmin(adminEmail);
//    }
//}
