package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Credentials2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredentialsRepository extends JpaRepository<Credentials2, Integer> {

    Optional<Credentials2> findByEmail(String email);

    boolean existsByEmail(String email);
}
