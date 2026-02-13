package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Minio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MinioRepository extends JpaRepository<Minio,String> {
}
