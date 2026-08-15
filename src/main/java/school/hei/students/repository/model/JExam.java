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
@Table(name = "exam")
public class JExam {
  @Id @GeneratedValue private UUID id;

  @Column(name = "assignment_id", nullable = false)
  private UUID assignmentId;

  @Column(nullable = false)
  private String label;

  @Column(name = "exam_date", nullable = false)
  private Instant examDate;

  @Column(nullable = false)
  private Double coefficient;

  @Column(nullable = false, length = 20)
  private String type;
}
