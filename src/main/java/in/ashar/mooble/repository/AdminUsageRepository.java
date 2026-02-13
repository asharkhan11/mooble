package in.ashar.mooble.repository;

import in.ashar.mooble.entity.AdminUsage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminUsageRepository extends JpaRepository<AdminUsage, Integer> {
    Optional<AdminUsage> findByAdminAdminId(int adminId);
}
