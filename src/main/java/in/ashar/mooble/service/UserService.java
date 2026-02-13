//package in.ashar.mooble.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import in.ashar.mooble.configuration.AppProperties;
//import in.ashar.mooble.exception.NotFoundException;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;
//
//import java.time.LocalDateTime;
//import java.util.List;
//
//@Service
//public class UserService {
//
//    @Autowired
//    private AppProperties appProperties;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    private final UserRepository userRepository;
//    private final PasswordSetupTokenService tokenService;
//    private final EmailService emailService;
//
//
//    public UserService(UserRepository userRepository,
//                       PasswordSetupTokenService tokenService,
//                       EmailService emailService) {
//        this.userRepository = userRepository;
//        this.tokenService = tokenService;
//        this.emailService = emailService;
//    }
//
//    public User createWithoutPassword(UserDto userDto) {
//
//        User user = objectMapper.convertValue(userDto, User.class);
//
//        user.setRole(userDto.getRole());
//        user.setPassword(null);
//        user.setCreatedAt(LocalDateTime.now());
//
//        User savedUser = userRepository.save(user);
//
//        String token = tokenService.createToken(savedUser.getEmail());
//        String link = appProperties.getBaseUrl() + "/auth/set-password?token=" + token;
//
//        emailService.sendPasswordSetupEmail(savedUser.getEmail(), link);
//
//        return savedUser;
//    }
//
//
//    public List<User> getAll() {
//        return userRepository.findAll();
//    }
//
//    public User getById(Long id) {
//        return userRepository.findById(id).orElseThrow(()-> new NotFoundException("user not found"));
//    }
//
//    public User updateBasicDetails(Long id, UserDto userDto) {
//
//        User oldUser = getById(id);
//
//        oldUser.setName(userDto.getName());
//        oldUser.setPhoneNumber(userDto.getPhoneNumber());
//        oldUser.setAddress(userDto.getAddress());
//
//        return userRepository.save(oldUser);
//    }
//
//    public User updateCredentials(Long id, UserDto userDto) {
////        User oldUser = getById(id);
////        oldUser.setEmail(userDto.getEmail());
////        oldUser.setPassword();
//        return null;
//    }
//
//    public void delete(Long id) {
//        userRepository.deleteById(id);
//    }
//
//
//}
