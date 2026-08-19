package school.hei.students.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.endpoint.event.EventProducer;
import school.hei.students.mapper.TranscriptDeliveryMapper;
import school.hei.students.model.DeliveryStatus;
import school.hei.students.model.GradeActor;
import school.hei.students.model.Role;
import school.hei.students.repository.StudentRepository;
import school.hei.students.repository.TranscriptDeliveryRepository;
import school.hei.students.repository.model.JTranscriptDelivery;
import school.hei.students.service.exception.StudentNotFoundException;

@ExtendWith(MockitoExtension.class)
class TranscriptDeliveryServiceTest {
  @Mock private TranscriptDeliveryRepository repository;
  @Mock private StudentRepository studentRepository;
  @Mock private GradeAuthorizationService authorizationService;
  @Mock private EventProducer eventProducer;
  private TranscriptDeliveryService service;
  private UUID studentId;
  private GradeActor admin;

  @BeforeEach
  void setUp() {
    service =
        new TranscriptDeliveryService(
            repository,
            new TranscriptDeliveryMapper(),
            studentRepository,
            authorizationService,
            eventProducer);
    studentId = UUID.randomUUID();
    admin = GradeActor.builder().role(Role.ADMIN).build();
  }

  @Test
  void request_transcript_creates_in_progress_delivery_and_fires_event() {
    doNothing().when(authorizationService).assertCanReadStudentGrades(admin, studentId);
    when(studentRepository.existsById(studentId)).thenReturn(true);
    when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    var delivery = service.requestTranscript(studentId, "2024-2025", admin);
    assertThat(delivery.studentId()).isEqualTo(studentId);
    assertThat(delivery.academicYear()).isEqualTo("2024-2025");
    assertThat(delivery.status()).isEqualTo(DeliveryStatus.IN_PROGRESS);
    assertThat(delivery.sentAt()).isNull();
    verify(eventProducer).accept(anyList());
  }

  @Test
  void request_transcript_fails_when_academic_year_missing() {
    doNothing().when(authorizationService).assertCanReadStudentGrades(admin, studentId);
    assertThatThrownBy(() -> service.requestTranscript(studentId, " ", admin))
        .isInstanceOf(ResponseStatusException.class);
    verify(eventProducer, never()).accept(anyList());
  }

  @Test
  void request_transcript_fails_when_student_not_found() {
    doNothing().when(authorizationService).assertCanReadStudentGrades(admin, studentId);
    when(studentRepository.existsById(studentId)).thenReturn(false);
    assertThatThrownBy(() -> service.requestTranscript(studentId, "2024-2025", admin))
        .isInstanceOf(StudentNotFoundException.class);
    verify(eventProducer, never()).accept(anyList());
  }

  @Test
  void request_transcript_fails_when_actor_not_authorized() {
    var otherStudent = GradeActor.builder().role(Role.STUDENT).studentId(UUID.randomUUID()).build();
    doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"))
        .when(authorizationService)
        .assertCanReadStudentGrades(otherStudent, studentId);
    assertThatThrownBy(() -> service.requestTranscript(studentId, "2024-2025", otherStudent))
        .isInstanceOf(ResponseStatusException.class);
    verify(eventProducer, never()).accept(anyList());
    verify(repository, never()).save(any());
  }

  @Test
  void get_history_for_student_fails_when_actor_not_authorized() {
    var otherStudent = GradeActor.builder().role(Role.STUDENT).studentId(UUID.randomUUID()).build();
    doThrow(new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"))
        .when(authorizationService)
        .assertCanReadStudentGrades(otherStudent, studentId);
    assertThatThrownBy(() -> service.getHistoryForStudent(studentId, otherStudent))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void get_history_for_student_returns_mapped_deliveries() {
    doNothing().when(authorizationService).assertCanReadStudentGrades(admin, studentId);
    when(studentRepository.existsById(studentId)).thenReturn(true);
    var entity =
        JTranscriptDelivery.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .academicYear("2024-2025")
            .status(DeliveryStatus.SENT.name())
            .build();
    when(repository.findByStudentIdOrderBySentAtDesc(studentId)).thenReturn(List.of(entity));
    var history = service.getHistoryForStudent(studentId, admin);
    assertThat(history).hasSize(1);
    assertThat(history.get(0).status()).isEqualTo(DeliveryStatus.SENT);
  }
}
