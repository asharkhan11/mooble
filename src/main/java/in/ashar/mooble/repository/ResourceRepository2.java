package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Resource2;
import in.ashar.mooble.entity.ResourceFolder2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ResourceRepository2 extends JpaRepository<Resource2, Integer> {

    List<Resource2> findByFolder_FolderId(int folderId);

    List<Resource2> findByTuitionIdAndCourseIdAndFolderIsNull(
            int tuitionId, Integer courseId
    );

    List<Resource2> findByTuitionIdAndSubjectIdAndFolderIsNull(
            int tuitionId, Integer subjectId
    );

    // all resources in folders
    List<Resource2> findByFolderFolderIdIn(List<Integer> folderIds);

    List<Resource2> findAllByTuitionId(int tuitionId);


    @Query("SELECT r.fileName, r.size FROM Resource2 r WHERE r.tuitionId = :id")
    List<Object[]> findFileNamesAndSizesByTuitionId(int id);

    @Modifying
    @Query("DELETE FROM Resource2 r WHERE r.tuitionId = :id")
    void deleteAllByTuitionId(int id);


}
