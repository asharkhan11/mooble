package in.ashar.mooble.repository;

import in.ashar.mooble.entity.ResourceFolder2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceFolderRepository2 extends JpaRepository<ResourceFolder2, Integer> {
    List<ResourceFolder2> findByTuitionIdAndParentFolderIsNullAndCourseId(
            int tuitionId, Integer courseId
    );

    List<ResourceFolder2> findByTuitionIdAndParentFolderIsNullAndSubjectId(
            int tuitionId, Integer subjectId
    );

    List<ResourceFolder2> findByParentFolder_FolderId(int parentFolderId);

    // all folders in subtree (materialized path)
    List<ResourceFolder2> findByPathStartingWith(String path);

    @Modifying
    @Query("DELETE FROM ResourceFolder2 f WHERE f.tuitionId = :id")
    void deleteAllByTuitionId(int id);
}
