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
@Table(name = "course_assignment")
public class JCourseAssignment {
  @Id @GeneratedValue private UUID id;

  @Column(name = "course_id", nullable = false)
  private UUID courseId;

  @Column(name = "academic_year", nullable = false, length = 20)
  private String academicYear;
}
