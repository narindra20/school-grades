package school.hei.students.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.students.repository.model.JStudentGroupHistory;

@Repository
public interface StudentGroupHistoryRepository extends JpaRepository<JStudentGroupHistory, UUID> {
  List<JStudentGroupHistory> findByStudentId(UUID studentId);

  List<JStudentGroupHistory> findByGroupId(UUID groupId);

  List<JStudentGroupHistory> findByStudentIdAndEndDateIsNull(UUID studentId);
}
