package school.hei.students.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.students.repository.model.JCourseAssignment;

@Repository
public interface CourseAssignmentRepository extends JpaRepository<JCourseAssignment, UUID> {
  List<JCourseAssignment> findByCourseId(UUID courseId);

  List<JCourseAssignment> findByAcademicYear(String academicYear);
}
