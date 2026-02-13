package in.ashar.mooble.service;

import in.ashar.mooble.dto.*;
import in.ashar.mooble.entity.Admin2;
import in.ashar.mooble.entity.AdminSubscription;
import in.ashar.mooble.entity.SubscriptionPlan;
import in.ashar.mooble.repository.Admin2Repository;
import in.ashar.mooble.repository.AdminSubscriptionRepository;
import in.ashar.mooble.repository.SubscriptionPlanRepository;
import in.ashar.mooble.repository.SuperUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SuperUserService {


    private final Admin2Repository adminRepository;
    private final SuperUserRepository superUserRepository;
    private final AdminSubscriptionRepository adminSubscriptionRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public List<SubscriptionAdminResponse> getAllSubscription() {
        return superUserRepository.getAllSubscriptionAdmin();
    }



    @Transactional
    public void changeAdminPlan(ChangeAdminPlanRequest request) {

        // 1. Load admin
        Admin2 admin = adminRepository.findById(request.getAdminId())
                .orElseThrow(() -> new RuntimeException("Admin not found"));

        // 2. Load plan
        SubscriptionPlan newPlan = subscriptionPlanRepository.findByName(request.getPlanName())
                .orElseThrow(() -> new RuntimeException("Plan not found"));

        // 3. Load or create subscription
        AdminSubscription subscription = adminSubscriptionRepository
                .findByAdminAdminId(admin.getAdminId())
                .orElseGet(() -> {
                    AdminSubscription s = new AdminSubscription();
                    s.setAdmin(admin);
                    s.setStartDate(LocalDate.now());
                    return s;
                });

        LocalDate today = LocalDate.now();

        // 4. If previous subscription expired, reset start date
        if (subscription.getEndDate() != null && subscription.getEndDate().isBefore(today)) {
            subscription.setStartDate(today);
        }

        // 5. Assign new plan
        subscription.setPlan(newPlan);
        subscription.setActive(true);

        // 6. Set / extend end date
        if (request.getDurationMonths() == null) {
            // Lifetime plan
            subscription.setEndDate(null);
        } else {
            LocalDate baseDate;

            if (subscription.getEndDate() == null || subscription.getEndDate().isBefore(today)) {
                baseDate = today;
            } else {
                baseDate = subscription.getEndDate();
            }

            subscription.setEndDate(baseDate.plusMonths(request.getDurationMonths()));
        }

        // 7. Save
        adminSubscriptionRepository.save(subscription);
    }




//    private MapObjects mapObjects;
//    private ObjectMapper objectMapper;
//    private RoleRepository roleRepository;
//    private Admin2Repository adminRepository;
//    private EmailService emailService;
//    private Tuition2Repository tuitionRepository;
//    private CredentialsRepository credentialsRepository;
//    private AdminHelper adminHelper;


//    @Transactional
//    public Tuition2ResponseDto createTuition(Tuition2Dto tuitionDto) {
//
//        Tuition2 tuition = objectMapper.convertValue(tuitionDto, Tuition2.class);
//        Admin2 admin = adminRepository.findById(tuitionDto.getAdminId()).orElseThrow(() -> new NotFoundException("Admin not found with id : " + tuitionDto.getAdminId()));
//
//        tuition.setTuitionAdmin(admin);
//
//        List<TuitionClass> tuitionClasses = Arrays.stream(Standard.values()).map(s -> TuitionClass.builder()
//                .tuition(tuition).standard(s).section('A').build()).toList();
//
//        tuition.setTuitionClasses(tuitionClasses);
//
//        Tuition2 saved = tuitionRepository.save(tuition);
//
//        // add to admin
//        List<Tuition2> adminTuition = Optional.ofNullable(admin.getAdminTuition()).orElse(new ArrayList<>());
//        adminTuition.add(saved);
//        admin.setAdminTuition(adminTuition);
//        adminRepository.save(admin);
//
//
//        return mapObjects.mapTuitionResponse(saved);
//    }
//
//
//    @Transactional
//    public Admin2ResponseDto createAdmin(Admin2Dto adminDto) {
//
//        Admin2 admin = objectMapper.convertValue(adminDto, Admin2.class);
//
//        String adminEmail = adminDto.getAdminEmail();
//
//        if(credentialsRepository.existsByEmail(adminEmail)){
//            throw new AlreadyExists("user with same email already exists");
//        }
//
//        Credentials2 credential = adminHelper.createCredentialWithoutPassword(adminEmail, List.of(in.ashar.mooble.utility.enums.Role.ADMIN.name(), in.ashar.mooble.utility.enums.Role.TEACHER.name()));
//
//        admin.setAdminCredential(credential);
//
//        Admin2 saved = adminRepository.save(admin);
//
//        Admin2ResponseDto response = mapObjects.mapAdminResponse(saved);
//
//        emailService.sendPasswordSetupEmail(saved.getAdminEmail());
//
//        return response;
//
//    }


//
//    public List<Admin2ResponseDto> getAllAdmins(){
//        List<Admin2> admins = adminRepository.findAll();
//
//        return admins.stream()
//                .map(mapObjects::mapAdminResponse)
//                .toList();
//    }
//
//
//    public List<Admin2ResponseDto> getAllAdminsWithNullPassword(){
//        List<Admin2> admins = adminRepository.findAllByAdminCredentialPassword(null);
//
//        return admins.stream()
//                .map(mapObjects::mapAdminResponse)
//                .toList();
//    }
//
//    public Admin2ResponseDto getAdminById(int id){
//        Admin2 admin = adminRepository.findById(id).orElseThrow(() -> new NotFoundException("Admin not found with id : " + id));
//
//        return mapObjects.mapAdminResponse(admin);
//    }
//
//    public List<Tuition2ResponseDto> getAllTuition() {
//        List<Tuition2> tuition = tuitionRepository.findAll();
//
//        return tuition.stream().map(mapObjects::mapTuitionResponse).toList();
//    }
//
//    public void deleteAdminById(int adminId) {
//        adminRepository.deleteById(adminId);
//    }
//
//    public Tuition2ResponseDto getTuitionById(int tuitionId){
//        Tuition2 tuition = tuitionRepository.findById(tuitionId).orElseThrow(() -> new NotFoundException("Tuition not found with id : " + tuitionId));
//
//        return mapObjects.mapTuitionResponse(tuition);
//    }
//
//    public void deleteTuitionId(int tuitionId){
//        tuitionRepository.deleteById(tuitionId);
//    }
//


}
