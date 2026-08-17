package school.hei.students.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.students.repository.model.JCourseAssignmentTeaching;

@Repository
public interface CourseAssignmentTeachingRepository
    extends JpaRepository<JCourseAssignmentTeaching, UUID> {
  List<JCourseAssignmentTeaching> findByAssignmentId(UUID assignmentId);

  List<JCourseAssignmentTeaching> findByTeacherId(UUID teacherId);

  List<JCourseAssignmentTeaching> findByGroupId(UUID groupId);
}
