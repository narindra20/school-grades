package school.hei.students.repository;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.students.repository.model.JCourse;

@Repository
public interface CourseRepository extends JpaRepository<JCourse, UUID> {}
