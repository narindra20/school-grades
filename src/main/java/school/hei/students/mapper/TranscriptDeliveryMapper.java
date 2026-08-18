package school.hei.students.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.students.model.DeliveryStatus;
import school.hei.students.model.TranscriptDelivery;
import school.hei.students.repository.model.JTranscriptDelivery;

@Component
public class TranscriptDeliveryMapper {
  public TranscriptDelivery toModel(JTranscriptDelivery entity) {
    return TranscriptDelivery.builder()
        .id(entity.getId())
        .studentId(entity.getStudentId())
        .academicYear(entity.getAcademicYear())
        .sentAt(entity.getSentAt())
        .s3Url(entity.getS3Url())
        .status(DeliveryStatus.valueOf(entity.getStatus()))
        .build();
  }

  public List<TranscriptDelivery> toModel(List<JTranscriptDelivery> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JTranscriptDelivery toEntity(TranscriptDelivery model) {
    return JTranscriptDelivery.builder()
        .id(model.id())
        .studentId(model.studentId())
        .academicYear(model.academicYear())
        .sentAt(model.sentAt())
        .s3Url(model.s3Url())
        .status(model.status().name())
        .build();
  }
}
