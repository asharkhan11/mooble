package in.ashar.mooble.repository;

import in.ashar.mooble.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Integer> {

    Optional<Attendance> findBySessionId(int sessionId);

    List<Attendance> findAllByMarkedByTeacherId(int teacherId);
}
