package in.ashar.mooble.repository;

import in.ashar.mooble.entity.AdminSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminSubscriptionRepository extends JpaRepository<AdminSubscription, Integer> {
    Optional<AdminSubscription> findByAdminAdminId(int adminId);
}
