package school.hei.students.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.students.repository.model.JExam;

@Repository
public interface ExamRepository extends JpaRepository<JExam, UUID> {
  List<JExam> findByAssignmentId(UUID assignmentId);
}
