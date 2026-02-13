package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionAdminResponse {

    private int adminId;
    private String adminName;
    private String adminEmail;
    private String adminPhoneNumber;

    private String planName;
    private int maxMembers;
    private long maxStorageMb;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean active;

    private int usedMembers;
    private long usedStorageKb;


}
