package in.ashar.mooble.repository;

import in.ashar.mooble.dto.SubscriptionAdminResponse;
import in.ashar.mooble.entity.Admin2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SuperUserRepository extends JpaRepository<Admin2, Integer> {


    @Query("""
            SELECT new in.ashar.mooble.dto.SubscriptionAdminResponse(
               a.adminId,
               a.adminName,
               a.adminEmail,
               a.adminPhoneNumber,
               p.name,
               p.maxMembers,
               p.maxStorageMb,
               s.startDate,
               s.endDate,
               s.active,
               u.usedMembers,
               u.usedStorageKb
            )
            FROM Admin2 a
            JOIN a.subscription s
            JOIN s.plan p
            JOIN a.usage u
            """)
    List<SubscriptionAdminResponse> getAllSubscriptionAdmin();


}
