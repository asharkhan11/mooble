package in.ashar.mooble.dto;

import lombok.Data;

@Data
public class ChangeAdminPlanRequest {

    private int adminId;
    private String planName; // FREE, ELITE, ENTERPRISE
    private Integer durationMonths; // null = lifetime
}
