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
@Table(name = "course")
public class JCourse {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, unique = true, length = 50)
  private String code;

  @Column(nullable = false)
  private String title;

  @Column(nullable = false)
  private Integer credits;

  @Column(nullable = false, length = 10)
  private String level;

  @Column(nullable = false, length = 10)
  private String semester;
}
