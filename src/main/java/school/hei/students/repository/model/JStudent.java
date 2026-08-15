package school.hei.students.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@Entity
@Table(name = "student")
public class JStudent {
  @Id @GeneratedValue private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "cohort_id", nullable = false)
  private UUID cohortId;

  @Column(name = "last_name", nullable = false, length = 200)
  private String lastName;

  @Column(name = "first_name", nullable = false, length = 200)
  private String firstName;

  @Column(name = "student_number", nullable = false, unique = true, length = 50)
  private String studentNumber;

  @Column(name = "work_study", nullable = false)
  private boolean workStudy;

  @Column(nullable = false)
  private boolean active;
}
