package in.ashar.mooble.controller;


import in.ashar.mooble.dto.*;
import in.ashar.mooble.service.SuperUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated
@RestController
@RequestMapping("/super-user")
@PreAuthorize("hasRole('SUPER_USER')")
@RequiredArgsConstructor
public class SuperUserController {

    private final SuperUserService superUserService;


    @GetMapping("/admin")
    public ResponseEntity<List<SubscriptionAdminResponse>> getAllSubscriptions(){
        return  ResponseEntity.ok(superUserService.getAllSubscription());
    }


    @PostMapping("/change-plan")
    public ResponseEntity<String> changeAdminPlan(@RequestBody ChangeAdminPlanRequest request) {
        superUserService.changeAdminPlan(request);
        return ResponseEntity.ok("Subscription updated successfully");
    }

//
//    @PostMapping("/admin")
//    public ResponseEntity<Admin2ResponseDto> createAdmin(@RequestBody @Valid Admin2Dto adminDto){
//        return ResponseEntity.ok(superUserService.createAdmin(adminDto));
//    }
//
//    @PostMapping("/tuition")
//    public ResponseEntity<Tuition2ResponseDto> createTuition(@RequestBody @Valid Tuition2Dto tuitionDto){
//        return ResponseEntity.ok(superUserService.createTuition(tuitionDto));
//    }
//
//    @GetMapping("/admin")
//    public ResponseEntity<List<Admin2ResponseDto>> getAllAdmins(){
//        return ResponseEntity.ok(superUserService.getAllAdmins());
//    }
//
//    @GetMapping("/admin/{adminId}")
//    public ResponseEntity<Admin2ResponseDto> getAdminById(@PathVariable("adminId") int adminId){
//        return ResponseEntity.ok(superUserService.getAdminById(adminId));
//    }
//
//    @GetMapping("/admin/null")
//    public ResponseEntity<List<Admin2ResponseDto>> getAllAdminsWithNullPassword(){
//        return ResponseEntity.ok(superUserService.getAllAdminsWithNullPassword());
//    }
//
//    @GetMapping("/tuition")
//    public ResponseEntity<List<Tuition2ResponseDto>> getAllTuition(){
//        return ResponseEntity.ok(superUserService.getAllTuition());
//    }
//
//    @GetMapping("/tuition/{tuitionId}")
//    public ResponseEntity<Tuition2ResponseDto> getTuitionById(@PathVariable("tuitionId") int tuitionId){
//        return ResponseEntity.ok(superUserService.getTuitionById(tuitionId));
//    }
//
//    @DeleteMapping("/admin/{adminId}")
//    public ResponseEntity<Void> deleteAdminById(@PathVariable("adminId") int adminId){
//        superUserService.deleteAdminById(adminId);
//        return ResponseEntity.noContent().build();
//    }
//
//    @DeleteMapping("/tuition/{tuitionId}")
//    public ResponseEntity<Void> deleteTuitionById(@PathVariable("tuitionId") int tuitionId){
//        superUserService.deleteTuitionId(tuitionId);
//        return ResponseEntity.noContent().build();
//    }




}
