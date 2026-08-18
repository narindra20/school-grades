package school.hei.students.repository;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import school.hei.students.repository.model.JTranscriptDelivery;

@Repository
public interface TranscriptDeliveryRepository extends JpaRepository<JTranscriptDelivery, UUID> {
  List<JTranscriptDelivery> findByStudentIdOrderBySentAtDesc(UUID studentId);

  List<JTranscriptDelivery> findAllByOrderBySentAtDesc();
}
