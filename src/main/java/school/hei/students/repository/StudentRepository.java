package school.hei.students.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.students.repository.model.JStudent;

@Repository
public interface StudentRepository extends JpaRepository<JStudent, UUID> {
  List<JStudent> findByCohortId(UUID cohortId);
  Optional<JStudent> findByUserId(UUID userId);
}
