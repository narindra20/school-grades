package school.hei.students.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.students.repository.model.JGradeHistory;

@Repository
public interface GradeHistoryRepository extends JpaRepository<JGradeHistory, UUID> {
  List<JGradeHistory> findByGradeIdOrderByModifiedAtDesc(UUID gradeId);
}
