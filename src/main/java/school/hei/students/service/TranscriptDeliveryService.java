package school.hei.students.service;

import static java.util.UUID.randomUUID;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.endpoint.event.EventProducer;
import school.hei.students.endpoint.event.model.TranscriptRequested;
import school.hei.students.mapper.TranscriptDeliveryMapper;
import school.hei.students.model.DeliveryStatus;
import school.hei.students.model.GradeActor;
import school.hei.students.model.TranscriptDelivery;
import school.hei.students.repository.StudentRepository;
import school.hei.students.repository.TranscriptDeliveryRepository;
import school.hei.students.service.exception.StudentNotFoundException;
import school.hei.students.service.exception.TranscriptDeliveryNotFoundException;

@Service
@AllArgsConstructor
public class TranscriptDeliveryService {
  private final TranscriptDeliveryRepository repository;
  private final TranscriptDeliveryMapper mapper;
  private final StudentRepository studentRepository;
  private final GradeAuthorizationService authorizationService;
  private final EventProducer eventProducer;

  @Transactional
  public TranscriptDelivery requestTranscript(
      UUID studentId, String academicYear, GradeActor actor) {
    authorizationService.assertCanReadStudentGrades(actor, studentId);
    assertAcademicYearProvided(academicYear);
    if (!studentRepository.existsById(studentId)) {
      throw new StudentNotFoundException(studentId);
    }
    var delivery =
        TranscriptDelivery.builder()
            .id(randomUUID())
            .studentId(studentId)
            .academicYear(academicYear)
            .sentAt(null)
            .s3Url(null)
            .status(DeliveryStatus.IN_PROGRESS)
            .build();
    var saved = mapper.toModel(repository.save(mapper.toEntity(delivery)));

    eventProducer.accept(
        List.of(
            TranscriptRequested.builder()
                .transcriptDeliveryId(saved.id())
                .studentId(studentId)
                .academicYear(academicYear)
                .build()));

    return saved;
  }

  public List<TranscriptDelivery> getHistoryForStudent(UUID studentId, GradeActor actor) {
    authorizationService.assertCanReadStudentGrades(actor, studentId);
    if (!studentRepository.existsById(studentId)) {
      throw new StudentNotFoundException(studentId);
    }
    return mapper.toModel(repository.findByStudentIdOrderBySentAtDesc(studentId));
  }

  public List<TranscriptDelivery> getAllHistory() {
    return mapper.toModel(repository.findAllByOrderBySentAtDesc());
  }

  public TranscriptDelivery getById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new TranscriptDeliveryNotFoundException(id)));
  }

  private void assertAcademicYearProvided(String academicYear) {
    if (academicYear == null || academicYear.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "academicYear is mandatory");
    }
  }
}
