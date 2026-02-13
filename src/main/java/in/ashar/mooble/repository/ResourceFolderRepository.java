package in.ashar.mooble.repository;

import in.ashar.mooble.entity.ResourceFolder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceFolderRepository extends JpaRepository<ResourceFolder, Integer> {

    List<ResourceFolder> findAllByCourseId(int courseId);

    List<ResourceFolder> findAllBySubjectId(int id);

    @Query("SELECT DISTINCT rf FROM ResourceFolder rf JOIN rf.resources r WHERE r.id IN :resourceIds")
    List<ResourceFolder> findAllByResourceIds(@Param("resourceIds") List<Integer> resourceIds);

}
