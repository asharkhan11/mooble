package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {
//
//
//    @Query("""
//                SELECT s FROM Session s
//                WHERE s.teacher.teacherId = :teacherId
//                  AND s.date BETWEEN CURRENT_DATE AND :endDate
//                  AND s.status = 'PLANNED'
//                ORDER BY s.date ASC, s.startTime ASC
//            """)
//    List<Session> findUpcomingSessionsForTeacher(
//            @Param("teacherId") int teacherId,
//            @Param("endDate") LocalDate endDate);
//
//    @Query("""
//            SELECT s FROM Session s
//            WHERE s.subject.subjectId IN :subjectIds
//              AND s.date BETWEEN CURRENT_DATE AND :endDate
//              AND s.status = 'PLANNED'
//            ORDER BY s.date ASC, s.startTime ASC
//            """)
//    List<Session> findUpcomingSessionsForStudentMultipleSubjects(
//            @Param("subjectIds") List<Integer> subjectIds,
//            @Param("endDate") LocalDate endDate);
//
//
//    // conflict check: sessions for the same teacher overlapping the given time
//    @Query("SELECT s FROM Session s WHERE s.date = :date AND s.teacher.id = :teacherId " +
//            "AND NOT (s.endTime <= :startTime OR s.startTime >= :endTime)")
//    List<Session> findConflictsForTeacher(@Param("date") LocalDate date,
//                                          @Param("teacherId") int teacherId,
//                                          @Param("startTime") LocalTime startTime,
//                                          @Param("endTime") LocalTime endTime);
//
//    // conflict check for tuition class (student group)
//    @Query("SELECT s FROM Session s WHERE s.date = :date AND s.tuitionClass.id = :tuitionClassId " +
//            "AND NOT (s.endTime <= :startTime OR s.startTime >= :endTime)")
//    List<Session> findConflictsForTuitionClass(@Param("date") LocalDate date,
//                                               @Param("tuitionClassId") int tuitionClassId,
//                                               @Param("startTime") LocalTime startTime,
//                                               @Param("endTime") LocalTime endTime);


    /// New Queries

    List<Session> findAllByRecurrenceGroupId(String recurrenceGroupId);

    @Query("""
            SELECT CASE WHEN EXISTS (
                SELECT 1 FROM Session s
                WHERE s.date = :date
                AND s.teacher.teacherId = :teacherId
                AND s.startTime < :endTime
                AND s.endTime > :startTime
                AND s.status = in.ashar.mooble.utility.enums.SessionStatus.PLANNED
                AND (:excludeSessionId IS NULL OR s.id <> :excludeSessionId)
            ) THEN true ELSE false END
            """)
    boolean existsTeacherConflict(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Integer teacherId,
            Integer excludeSessionId
    );


    @Query("""
            SELECT CASE WHEN EXISTS (
                SELECT 1 FROM Session s
                WHERE s.date = :date
                AND s.tuitionClass.id = :tuitionClassId
                AND s.subject IS NOT NULL
                AND s.startTime < :endTime
                AND s.endTime > :startTime
                AND s.status = in.ashar.mooble.utility.enums.SessionStatus.PLANNED
                AND (:excludeSessionId IS NULL OR s.id <> :excludeSessionId)
            ) THEN true ELSE false END
            """)
    boolean existsTuitionClassConflict(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Integer tuitionClassId,
            Integer excludeSessionId
    );


    @Query("""
            SELECT CASE WHEN EXISTS (
                SELECT 1 FROM Session s
                WHERE s.date = :date
                AND s.startTime < :endTime
                AND s.endTime > :startTime
                AND s.status = in.ashar.mooble.utility.enums.SessionStatus.PLANNED
                AND (
                    s.course.courseId = :courseId
                    OR s.subject.subjectId IN :courseSubjectIds
                )
                AND (:excludeSessionId IS NULL OR s.id <> :excludeSessionId)
            ) THEN true ELSE false END
            """)
    boolean existsCourseConflict(
            LocalDate date,
            LocalTime startTime,
            LocalTime endTime,
            Integer courseId,
            List<Integer> courseSubjectIds,
            Integer excludeSessionId
    );


    @Query("""
            SELECT s FROM Session s
            WHERE s.date BETWEEN :startDate AND :endDate
            AND s.status = in.ashar.mooble.utility.enums.SessionStatus.PLANNED
            ORDER BY s.date, s.startTime
            """)
    List<Session> findByDateRange(
            LocalDate startDate,
            LocalDate endDate
    );


    @Query("""
            SELECT s FROM Session s
            WHERE s.date BETWEEN :startDate AND :endDate
            AND s.createdBy = :adminId
            AND s.status = in.ashar.mooble.utility.enums.SessionStatus.PLANNED
            ORDER BY s.date, s.startTime
            """)
    List<Session> findByDateRangeAndAdmin(
            LocalDate startDate,
            LocalDate endDate,
            Integer adminId
    );


    @Query("""
            SELECT s FROM Session s
            WHERE s.date BETWEEN :startDate AND :endDate
            AND s.teacher.teacherId = :teacherId
            AND s.status = in.ashar.mooble.utility.enums.SessionStatus.PLANNED
            ORDER BY s.date, s.startTime
            """)
    List<Session> findByDateRangeAndTeacher(
            LocalDate startDate,
            LocalDate endDate,
            Integer teacherId
    );


    @Query("""
            SELECT s FROM Session s
            WHERE s.date BETWEEN :startDate AND :endDate
            AND s.status = in.ashar.mooble.utility.enums.SessionStatus.PLANNED
            AND (
                (s.subject IS NOT NULL AND s.subject.subjectId IN :subjectIds)
                OR
                (s.course IS NOT NULL AND s.course.courseId IN :courseIds)
            )
            ORDER BY s.date, s.startTime
            """)
    List<Session> findByDateRangeAndStudent(
            LocalDate startDate,
            LocalDate endDate,
            List<Integer> subjectIds,
            List<Integer> courseIds
    );


}