package school.hei.students.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
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
@Table(name = "grade_history")
public class JGradeHistory {
  @Id @GeneratedValue private UUID id;

  @Column(name = "grade_id", nullable = false)
  private UUID gradeId;

  @Column(name = "old_value", nullable = false)
  private Double oldValue;

  @Column(name = "new_value", nullable = false)
  private Double newValue;

  @Column(nullable = false, length = 500)
  private String reason;

  @Column(name = "modified_at", nullable = false)
  private Instant modifiedAt;
}
