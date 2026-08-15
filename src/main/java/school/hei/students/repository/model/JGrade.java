package school.hei.students.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
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
@Table(name = "grade")
public class JGrade {
  @Id @GeneratedValue private UUID id;

  @Column(name = "student_id", nullable = false)
  private UUID studentId;

  @Column(name = "exam_id", nullable = false)
  private UUID examId;

  @Column(nullable = false)
  private Double value;

  @Column(name = "graded_date", nullable = false)
  private LocalDate gradedDate;
}
