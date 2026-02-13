package in.ashar.mooble.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SubscriptionPlanResponse {

    private int subscriptionId;
    private String planName;
    private int price;
    private int maxMembers;
    private long maxStorageMb;

}
