package school.hei.students.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.students.repository.model.JCohort;

@Repository
public interface CohortRepository extends JpaRepository<JCohort, UUID> {}
