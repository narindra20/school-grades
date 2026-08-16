package school.hei.students.repository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.students.repository.model.JUser;

@Repository
public interface UserRepository extends JpaRepository<JUser, UUID> {
  Optional<JUser> findByEmail(String email);
}
