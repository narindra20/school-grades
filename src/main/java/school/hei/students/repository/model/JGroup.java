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
@Table(name = "\"group\"")
public class JGroup {
  @Id @GeneratedValue private UUID id;

  @Column(nullable = false, length = 50)
  private String code;

  @Column(name = "track_id")
  private UUID trackId;

  @Column(name = "cohort_id", nullable = false)
  private UUID cohortId;
}
