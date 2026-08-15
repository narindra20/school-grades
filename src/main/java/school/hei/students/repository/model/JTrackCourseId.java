package school.hei.students.repository.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
@Embeddable
public class JTrackCourseId implements Serializable {
  @Column(name = "track_id")
  private UUID trackId;

  @Column(name = "course_id")
  private UUID courseId;
}
