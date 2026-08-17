package school.hei.students.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.students.repository.model.JGroup;

@Repository
public interface GroupRepository extends JpaRepository<JGroup, UUID> {
  List<JGroup> findByCohortId(UUID cohortId);

  List<JGroup> findByTrackId(UUID trackId);
}
