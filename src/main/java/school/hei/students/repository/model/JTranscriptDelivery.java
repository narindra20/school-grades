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
@Table(name = "transcript_delivery")
public class JTranscriptDelivery {
  @Id @GeneratedValue private UUID id;

  @Column(name = "student_id", nullable = false)
  private UUID studentId;

  @Column(name = "academic_year", nullable = false, length = 20)
  private String academicYear;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Column(name = "s3_url", length = 1000)
  private String s3Url;

  @Column(nullable = false, length = 20)
  private String status;
}
