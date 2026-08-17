package school.hei.students.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.students.repository.model.JTeacher;

@Repository
public interface TeacherRepository extends JpaRepository<JTeacher, UUID> {
  Optional<JTeacher> findByUserId(UUID userId);
}
