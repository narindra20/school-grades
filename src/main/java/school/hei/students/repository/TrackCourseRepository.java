package school.hei.students.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.students.repository.model.JTrackCourse;
import school.hei.students.repository.model.JTrackCourseId;

@Repository
public interface TrackCourseRepository extends JpaRepository<JTrackCourse, JTrackCourseId> {
  List<JTrackCourse> findByIdTrackId(java.util.UUID trackId);
}
