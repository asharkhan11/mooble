package in.ashar.mooble.service;

import in.ashar.mooble.entity.*;
import in.ashar.mooble.exception.PlanLimitExceededException;
import in.ashar.mooble.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionGuardService {

    private final AdminSubscriptionRepository subscriptionRepository;
    private final AdminUsageRepository usageRepository;

    private AdminSubscription getActiveSubscription(int adminId) {
        AdminSubscription sub = subscriptionRepository.findByAdminAdminId(adminId)
                .orElseThrow(() -> new IllegalStateException("No subscription found"));

        if (!sub.isActive()) {
            throw new PlanLimitExceededException("Subscription expired. Please renew.");
        }

        if (sub.getEndDate() != null && sub.getEndDate().isBefore(LocalDate.now())) {
            sub.setActive(false);
            subscriptionRepository.save(sub);
            throw new PlanLimitExceededException("Subscription expired. Please renew.");
        }

        return sub;
    }


    private AdminUsage getUsage(int adminId) {
        return usageRepository.findByAdminAdminId(adminId)
                .orElseThrow(() -> new IllegalStateException("Usage record not found"));
    }

    // ================== MEMBER CHECK ==================

    public void assertCanAddMember(Admin2 admin) {
        AdminSubscription sub = getActiveSubscription(admin.getAdminId());
        AdminUsage usage = getUsage(admin.getAdminId());

        int max = sub.getPlan().getMaxMembers();

        if (usage.getUsedMembers() >= max) {
            throw new PlanLimitExceededException(
                "Member limit reached for plan: " + sub.getPlan().getName()
            );
        }
    }

    public void onMemberAdded(Admin2 admin) {
        AdminUsage usage = getUsage(admin.getAdminId());
        usage.setUsedMembers(usage.getUsedMembers() + 1);
        usageRepository.save(usage);
    }

    public void onMemberRemoved(Admin2 admin) {
        AdminUsage usage = getUsage(admin.getAdminId());
        usage.setUsedMembers(Math.max(0, usage.getUsedMembers() - 1));
        usageRepository.save(usage);
    }

    // ================== STORAGE CHECK ==================

    public void assertCanUpload(Admin2 admin, long fileSizeBytes) {
        AdminSubscription sub = getActiveSubscription(admin.getAdminId());
        AdminUsage usage = getUsage(admin.getAdminId());

        long used = usage.getUsedStorageKb();
        long max = (long) (sub.getPlan().getMaxStorageMb() * 1024.0);

        long fileSizeKb = (long) (fileSizeBytes / 1024.0);

        if (used + fileSizeKb > max) {
            throw new PlanLimitExceededException(
                "Storage limit exceeded for plan: " + sub.getPlan().getName()
            );
        }
    }

    public void onFileUploaded(Admin2 admin, long fileSizeBytes) {
        AdminUsage usage = getUsage(admin.getAdminId());
        long fileSizeKb = (long) (fileSizeBytes / 1024.0);
        usage.setUsedStorageKb(usage.getUsedStorageKb() + fileSizeKb);
        usageRepository.save(usage);
    }

    public void onFileDeleted(Admin2 admin, long fileSizeBytes) {
        AdminUsage usage = getUsage(admin.getAdminId());
        long fileSizeKb = (long) (fileSizeBytes / 1024.0);
        usage.setUsedStorageKb(Math.max(0, usage.getUsedStorageKb() - fileSizeKb));
        usageRepository.save(usage);
    }
}
