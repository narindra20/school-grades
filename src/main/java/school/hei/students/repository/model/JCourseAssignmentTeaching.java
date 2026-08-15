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
@Table(name = "course_assignment_teaching")
public class JCourseAssignmentTeaching {
  @Id @GeneratedValue private UUID id;

  @Column(name = "assignment_id", nullable = false)
  private UUID assignmentId;

  @Column(name = "teacher_id", nullable = false)
  private UUID teacherId;

  @Column(name = "group_id", nullable = false)
  private UUID groupId;
}
