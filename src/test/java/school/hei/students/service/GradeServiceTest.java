package school.hei.students.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.endpoint.rest.controller.dto.GradeCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.GradeUpdateRequest;
import school.hei.students.mapper.GradeHistoryMapper;
import school.hei.students.mapper.GradeMapper;
import school.hei.students.model.ExamType;
import school.hei.students.model.GradeActor;
import school.hei.students.model.Role;
import school.hei.students.repository.ExamRepository;
import school.hei.students.repository.GradeHistoryRepository;
import school.hei.students.repository.GradeRepository;
import school.hei.students.repository.model.JExam;
import school.hei.students.repository.model.JGrade;
import school.hei.students.repository.model.JGradeHistory;

@ExtendWith(MockitoExtension.class)
class GradeServiceTest {
  @Mock private GradeRepository gradeRepository;
  @Mock private GradeHistoryRepository gradeHistoryRepository;
  @Mock private ExamRepository examRepository;
  @Mock private GradeAuthorizationService authorizationService;
  private GradeService gradeService;
  private UUID studentId;
  private UUID examId;
  private UUID assignmentId;
  private GradeActor admin;

  @BeforeEach
  void setUp() {
    gradeService =
        new GradeService(
            gradeRepository,
            gradeHistoryRepository,
            examRepository,
            new GradeMapper(),
            new GradeHistoryMapper(),
            authorizationService);
    studentId = UUID.randomUUID();
    examId = UUID.randomUUID();
    assignmentId = UUID.randomUUID();
    admin = GradeActor.builder().role(Role.ADMIN).build();
  }

  @Test
  void create_grade_rejects_value_above_20() {
    var request =
        GradeCreateRequest.builder().studentId(studentId).examId(examId).value(21.0).build();
    assertThatThrownBy(() -> gradeService.createGrade(studentId, request, admin))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void create_grade_rejects_mismatched_student_id() {
    var otherStudentId = UUID.randomUUID();
    var request =
        GradeCreateRequest.builder().studentId(otherStudentId).examId(examId).value(15.0).build();
    assertThatThrownBy(() -> gradeService.createGrade(studentId, request, admin))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void create_regular_grade_succeeds_for_admin() {
    var request =
        GradeCreateRequest.builder().studentId(studentId).examId(examId).value(15.0).build();
    var regularExam =
        JExam.builder().id(examId).assignmentId(assignmentId).type(ExamType.REGULAR.name()).build();
    when(examRepository.findById(examId)).thenReturn(Optional.of(regularExam));
    when(gradeRepository.save(any()))
        .thenAnswer(
            invocation -> {
              JGrade toSave = invocation.getArgument(0);
              toSave.setId(UUID.randomUUID());
              return toSave;
            });
    var result = gradeService.createGrade(studentId, request, admin);
    assertThat(result.value()).isEqualTo(15.0);
    assertThat(result.gradedDate()).isEqualTo(LocalDate.now());
  }

  @Test
  void create_resit_grade_rejected_when_no_regular_grade_exists() {
    var resitExamId = UUID.randomUUID();
    var request =
        GradeCreateRequest.builder().studentId(studentId).examId(resitExamId).value(12.0).build();
    var resitExam =
        JExam.builder()
            .id(resitExamId)
            .assignmentId(assignmentId)
            .type(ExamType.RESIT.name())
            .build();
    when(examRepository.findById(resitExamId)).thenReturn(Optional.of(resitExam));
    when(examRepository.findByAssignmentId(assignmentId)).thenReturn(List.of());
    assertThatThrownBy(() -> gradeService.createGrade(studentId, request, admin))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void create_resit_grade_rejected_when_regular_grade_already_passing() {
    var regularExamId = UUID.randomUUID();
    var resitExamId = UUID.randomUUID();
    var request =
        GradeCreateRequest.builder().studentId(studentId).examId(resitExamId).value(12.0).build();
    var regularExam =
        JExam.builder()
            .id(regularExamId)
            .assignmentId(assignmentId)
            .type(ExamType.REGULAR.name())
            .build();
    var resitExam =
        JExam.builder()
            .id(resitExamId)
            .assignmentId(assignmentId)
            .type(ExamType.RESIT.name())
            .build();
    var passingGrade =
        JGrade.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .examId(regularExamId)
            .value(11.0)
            .build();
    when(examRepository.findById(resitExamId)).thenReturn(Optional.of(resitExam));
    when(examRepository.findByAssignmentId(assignmentId)).thenReturn(List.of(regularExam));
    when(gradeRepository.findByStudentId(studentId)).thenReturn(List.of(passingGrade));
    assertThatThrownBy(() -> gradeService.createGrade(studentId, request, admin))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void create_resit_grade_allowed_when_regular_grade_failed() {
    var regularExamId = UUID.randomUUID();
    var resitExamId = UUID.randomUUID();
    var request =
        GradeCreateRequest.builder().studentId(studentId).examId(resitExamId).value(12.0).build();
    var regularExam =
        JExam.builder()
            .id(regularExamId)
            .assignmentId(assignmentId)
            .type(ExamType.REGULAR.name())
            .build();
    var resitExam =
        JExam.builder()
            .id(resitExamId)
            .assignmentId(assignmentId)
            .type(ExamType.RESIT.name())
            .build();
    var failingGrade =
        JGrade.builder()
            .id(UUID.randomUUID())
            .studentId(studentId)
            .examId(regularExamId)
            .value(8.0)
            .build();
    when(examRepository.findById(resitExamId)).thenReturn(Optional.of(resitExam));
    when(examRepository.findByAssignmentId(assignmentId)).thenReturn(List.of(regularExam));
    when(gradeRepository.findByStudentId(studentId)).thenReturn(List.of(failingGrade));
    when(gradeRepository.save(any()))
        .thenAnswer(
            invocation -> {
              JGrade toSave = invocation.getArgument(0);
              toSave.setId(UUID.randomUUID());
              return toSave;
            });
    var result = gradeService.createGrade(studentId, request, admin);
    assertThat(result.value()).isEqualTo(12.0);
  }

  @Test
  void update_grade_requires_reason() {
    var gradeId = UUID.randomUUID();
    var request = GradeUpdateRequest.builder().value(14.0).reason(" ").build();
    assertThatThrownBy(() -> gradeService.updateGrade(gradeId, request, admin))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void update_grade_creates_history_entry_with_reason() {
    var gradeId = UUID.randomUUID();
    var request =
        GradeUpdateRequest.builder().value(14.0).reason("Student dispute on question 3").build();
    var existing =
        JGrade.builder()
            .id(gradeId)
            .studentId(studentId)
            .examId(examId)
            .value(10.0)
            .gradedDate(LocalDate.now())
            .build();
    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(existing));
    when(gradeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    gradeService.updateGrade(gradeId, request, admin);
    var captor = org.mockito.ArgumentCaptor.forClass(JGradeHistory.class);
    org.mockito.Mockito.verify(gradeHistoryRepository).save(captor.capture());
    assertThat(captor.getValue().getOldValue()).isEqualTo(10.0);
    assertThat(captor.getValue().getNewValue()).isEqualTo(14.0);
    assertThat(captor.getValue().getReason()).isEqualTo("Student dispute on question 3");
  }

  @Test
  void list_grades_delegates_to_authorization_service() {
    when(gradeRepository.findByStudentId(studentId)).thenReturn(List.of());
    gradeService.listGradesForStudent(studentId, admin);
    org.mockito.Mockito.verify(authorizationService).assertCanReadStudentGrades(admin, studentId);
  }

  @Test
  void get_grade_returns_mapped_grade_when_authorized() {
    var gradeId = UUID.randomUUID();
    var entity =
        JGrade.builder()
            .id(gradeId)
            .studentId(studentId)
            .examId(examId)
            .value(16.0)
            .gradedDate(LocalDate.now())
            .build();
    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(entity));
    var result = gradeService.getGrade(gradeId, admin);
    assertThat(result.value()).isEqualTo(16.0);
    org.mockito.Mockito.verify(authorizationService).assertCanReadStudentGrades(admin, studentId);
  }

  @Test
  void get_grade_throws_not_found_when_missing() {
    var gradeId = UUID.randomUUID();
    when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> gradeService.getGrade(gradeId, admin))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void get_grade_history_returns_mapped_entries_when_authorized() {
    var gradeId = UUID.randomUUID();
    var entity =
        JGrade.builder()
            .id(gradeId)
            .studentId(studentId)
            .examId(examId)
            .value(12.0)
            .gradedDate(LocalDate.now())
            .build();
    var historyEntry =
        JGradeHistory.builder()
            .id(UUID.randomUUID())
            .gradeId(gradeId)
            .oldValue(10.0)
            .newValue(12.0)
            .reason("Correction")
            .modifiedAt(java.time.Instant.now())
            .build();
    when(gradeRepository.findById(gradeId)).thenReturn(Optional.of(entity));
    when(gradeHistoryRepository.findByGradeIdOrderByModifiedAtDesc(gradeId))
        .thenReturn(List.of(historyEntry));
    var history = gradeService.getGradeHistory(gradeId, admin);
    assertThat(history).hasSize(1);
    assertThat(history.get(0).reason()).isEqualTo("Correction");
    org.mockito.Mockito.verify(authorizationService).assertCanReadStudentGrades(admin, studentId);
  }

  @Test
  void get_grade_history_throws_not_found_when_grade_missing() {
    var gradeId = UUID.randomUUID();
    when(gradeRepository.findById(gradeId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> gradeService.getGradeHistory(gradeId, admin))
        .isInstanceOf(ResponseStatusException.class);
  }
}
