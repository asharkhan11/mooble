package in.ashar.mooble.repository;

import in.ashar.mooble.entity.ForgotPasswordOtp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForgotPasswordOtpRepository extends JpaRepository<ForgotPasswordOtp, Integer> {
    Optional<ForgotPasswordOtp> findByEmail(String email);

    Optional<ForgotPasswordOtp> findByEmailAndOtp(String email, String otp);
}
