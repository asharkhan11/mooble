package in.ashar.mooble.repository;

import in.ashar.mooble.entity.AssignmentResource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssignmentResourceRepository extends JpaRepository<AssignmentResource, Integer> {
}
