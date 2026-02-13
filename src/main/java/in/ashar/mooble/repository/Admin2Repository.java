package in.ashar.mooble.repository;

import in.ashar.mooble.dto.SubscriptionAdminResponse;
import in.ashar.mooble.entity.Admin2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface Admin2Repository extends JpaRepository<Admin2, Integer> {
    Optional<Admin2> findByAdminEmail(String adminEmail);

    List<Admin2> findAllByAdminCredentialPassword(String password);


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
    WHERE a.adminId = :adminId
    """)
    Optional<SubscriptionAdminResponse> getSubscriptionByAdminId(@Param("adminId") int adminId);

}
