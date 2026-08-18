package school.hei.students.service.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import school.hei.students.endpoint.event.model.TranscriptRequested;
import school.hei.students.file.bucket.BucketComponent;
import school.hei.students.mail.Email;
import school.hei.students.mail.Mailer;
import school.hei.students.model.DeliveryStatus;
import school.hei.students.pdf.TranscriptPdfGenerator;
import school.hei.students.repository.CourseAssignmentRepository;
import school.hei.students.repository.CourseRepository;
import school.hei.students.repository.ExamRepository;
import school.hei.students.repository.GradeRepository;
import school.hei.students.repository.StudentRepository;
import school.hei.students.repository.TranscriptDeliveryRepository;
import school.hei.students.repository.UserRepository;
import school.hei.students.repository.model.JStudent;
import school.hei.students.repository.model.JTranscriptDelivery;
import school.hei.students.repository.model.JUser;

@ExtendWith(MockitoExtension.class)
class TranscriptRequestedServiceTest {
  @Mock private TranscriptDeliveryRepository transcriptDeliveryRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private UserRepository userRepository;
  @Mock private CourseAssignmentRepository courseAssignmentRepository;
  @Mock private ExamRepository examRepository;
  @Mock private GradeRepository gradeRepository;
  @Mock private CourseRepository courseRepository;
  @Mock private TranscriptPdfGenerator pdfGenerator;
  @Mock private BucketComponent bucketComponent;
  @Mock private Mailer mailer;

  private TranscriptRequestedService service;
  private UUID deliveryId;
  private UUID studentId;

  @BeforeEach
  void setUp() {
    service =
        new TranscriptRequestedService(
            transcriptDeliveryRepository,
            studentRepository,
            userRepository,
            courseAssignmentRepository,
            examRepository,
            gradeRepository,
            courseRepository,
            pdfGenerator,
            bucketComponent,
            mailer);
    deliveryId = UUID.randomUUID();
    studentId = UUID.randomUUID();
  }

  @Test
  void already_sent_delivery_is_skipped_for_idempotency() {
    var delivery =
        JTranscriptDelivery.builder()
            .id(deliveryId)
            .studentId(studentId)
            .status(DeliveryStatus.SENT.name())
            .build();
    when(transcriptDeliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));

    service.accept(
        TranscriptRequested.builder()
            .transcriptDeliveryId(deliveryId)
            .studentId(studentId)
            .academicYear("2024-2025")
            .build());

    verify(studentRepository, never()).findById(any());
    verify(mailer, never()).accept(any());
    verify(transcriptDeliveryRepository, never()).save(any());
  }

  @Test
  void happy_path_generates_uploads_mails_and_marks_sent() throws Exception {
    var delivery =
        JTranscriptDelivery.builder()
            .id(deliveryId)
            .studentId(studentId)
            .academicYear("2024-2025")
            .status(DeliveryStatus.IN_PROGRESS.name())
            .build();
    var userId = UUID.randomUUID();
    var student =
        JStudent.builder()
            .id(studentId)
            .userId(userId)
            .lastName("Rakoto")
            .firstName("Fitia")
            .studentNumber("24001")
            .build();
    var user = JUser.builder().id(userId).email("fitia.rakoto@example.com").build();
    var pdfFile = File.createTempFile("transcript-test-", ".pdf");
    pdfFile.deleteOnExit();

    when(transcriptDeliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
    when(studentRepository.findById(studentId)).thenReturn(Optional.of(student));
    when(userRepository.findById(userId)).thenReturn(Optional.of(user));
    when(courseAssignmentRepository.findByAcademicYear("2024-2025")).thenReturn(List.of());
    when(gradeRepository.findByStudentId(studentId)).thenReturn(List.of());
    when(pdfGenerator.generate(any())).thenReturn(pdfFile);

    service.accept(
        TranscriptRequested.builder()
            .transcriptDeliveryId(deliveryId)
            .studentId(studentId)
            .academicYear("2024-2025")
            .build());

    verify(bucketComponent).upload(eq(pdfFile), any());
    var emailCaptor = ArgumentCaptor.forClass(Email.class);
    verify(mailer).accept(emailCaptor.capture());
    assertThat(emailCaptor.getValue().to().getAddress()).isEqualTo("fitia.rakoto@example.com");

    var savedCaptor = ArgumentCaptor.forClass(JTranscriptDelivery.class);
    verify(transcriptDeliveryRepository).save(savedCaptor.capture());
    assertThat(savedCaptor.getValue().getStatus()).isEqualTo(DeliveryStatus.SENT.name());
    assertThat(savedCaptor.getValue().getSentAt()).isNotNull();
  }

  @Test
  void failure_is_marked_and_rethrown_for_retry() {
    var delivery =
        JTranscriptDelivery.builder()
            .id(deliveryId)
            .studentId(studentId)
            .academicYear("2024-2025")
            .status(DeliveryStatus.IN_PROGRESS.name())
            .build();
    when(transcriptDeliveryRepository.findById(deliveryId)).thenReturn(Optional.of(delivery));
    when(studentRepository.findById(studentId)).thenReturn(Optional.empty());

    assertThatThrownBy(
            () ->
                service.accept(
                    TranscriptRequested.builder()
                        .transcriptDeliveryId(deliveryId)
                        .studentId(studentId)
                        .academicYear("2024-2025")
                        .build()))
        .isInstanceOf(RuntimeException.class);

    var savedCaptor = ArgumentCaptor.forClass(JTranscriptDelivery.class);
    verify(transcriptDeliveryRepository, times(1)).save(savedCaptor.capture());
    assertThat(savedCaptor.getValue().getStatus()).isEqualTo(DeliveryStatus.FAILED.name());
    verify(mailer, never()).accept(any());
  }
}
