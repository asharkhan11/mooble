package in.ashar.mooble.repository;

import in.ashar.mooble.entity.RegistrationOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RegistrationOtpRepository extends JpaRepository<RegistrationOtp, Integer> {
    Optional<RegistrationOtp> findByEmail(String email);

    Optional<RegistrationOtp> findByEmailAndOtp(String email, String otp);
}
