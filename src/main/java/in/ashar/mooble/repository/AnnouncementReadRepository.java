package in.ashar.mooble.repository;


import in.ashar.mooble.entity.AnnouncementRead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnnouncementReadRepository extends JpaRepository<AnnouncementRead, Integer> {
    
    Optional<AnnouncementRead> findByTeacherIdAndAnnouncementId(Integer teacherId, Integer announcementId);
    
    List<AnnouncementRead> findByTeacherIdAndTuitionId(Integer teacherId, Integer tuitionId);
    
    @Query("SELECT ar.announcementId FROM AnnouncementRead ar WHERE ar.teacherId = :teacherId AND ar.tuitionId = :tuitionId")
    List<Integer> findReadAnnouncementIdsByTeacherIdAndTuitionId(Integer teacherId, Integer tuitionId);
    
    boolean existsByTeacherIdAndAnnouncementId(Integer teacherId, Integer announcementId);


    @Modifying
    @Query("DELETE FROM AnnouncementRead ar WHERE ar.tuitionId = :id")
    void deleteAllByTuitionId(@Param("id") int id);

}
