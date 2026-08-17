package school.hei.students.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.endpoint.rest.controller.dto.ExamCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.ExamUpdateRequest;
import school.hei.students.mapper.ExamMapper;
import school.hei.students.model.ExamType;
import school.hei.students.model.GradeActor;
import school.hei.students.model.Role;
import school.hei.students.repository.ExamRepository;
import school.hei.students.repository.GradeRepository;
import school.hei.students.repository.model.JExam;
import school.hei.students.repository.model.JGrade;

@ExtendWith(MockitoExtension.class)
class ExamServiceTest {
  @Mock private ExamRepository examRepository;
  @Mock private GradeRepository gradeRepository;
  private ExamService examService;
  private UUID assignmentId;
  private GradeActor admin;
  private GradeActor teacher;

  @BeforeEach
  void setUp() {
    examService = new ExamService(examRepository, gradeRepository, new ExamMapper());
    assignmentId = UUID.randomUUID();
    admin = GradeActor.builder().role(Role.ADMIN).build();
    teacher = GradeActor.builder().role(Role.TEACHER).teacherId(UUID.randomUUID()).build();
  }

  @Test
  void teacher_cannot_create_exam() {
    var request =
        ExamCreateRequest.builder().label("OOP").examDate(Instant.now()).coefficient(0.6).build();
    assertThatThrownBy(() -> examService.createExam(assignmentId, request, teacher))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void create_exam_rejects_non_positive_coefficient() {
    var request =
        ExamCreateRequest.builder().label("OOP").examDate(Instant.now()).coefficient(0.0).build();
    assertThatThrownBy(() -> examService.createExam(assignmentId, request, admin))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void create_exam_defaults_to_regular_type() {
    var request =
        ExamCreateRequest.builder().label("OOP").examDate(Instant.now()).coefficient(0.6).build();
    when(examRepository.save(any()))
        .thenAnswer(
            invocation -> {
              JExam toSave = invocation.getArgument(0);
              toSave.setId(UUID.randomUUID());
              return toSave;
            });
    var result = examService.createExam(assignmentId, request, admin);
    assertThat(result.type()).isEqualTo(ExamType.REGULAR);
  }

  @Test
  void update_exam_admin_only() {
    var examId = UUID.randomUUID();
    var request =
        ExamUpdateRequest.builder().label("API").examDate(Instant.now()).coefficient(0.4).build();
    assertThatThrownBy(() -> examService.updateExam(examId, request, teacher))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void delete_exam_rejected_when_grades_linked() {
    var examId = UUID.randomUUID();
    var existing =
        JExam.builder().id(examId).assignmentId(assignmentId).type(ExamType.REGULAR.name()).build();
    when(examRepository.findById(examId)).thenReturn(Optional.of(existing));
    when(gradeRepository.findByExamId(examId))
        .thenReturn(List.of(JGrade.builder().id(UUID.randomUUID()).build()));
    assertThatThrownBy(() -> examService.deleteExam(examId, admin))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void delete_exam_allowed_when_no_grade_linked() {
    var examId = UUID.randomUUID();
    var existing =
        JExam.builder().id(examId).assignmentId(assignmentId).type(ExamType.REGULAR.name()).build();
    when(examRepository.findById(examId)).thenReturn(Optional.of(existing));
    when(gradeRepository.findByExamId(examId)).thenReturn(List.of());
    examService.deleteExam(examId, admin);
    org.mockito.Mockito.verify(examRepository).delete(existing);
  }

  @Test
  void delete_exam_throws_not_found_when_missing() {
    var examId = UUID.randomUUID();
    when(examRepository.findById(examId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> examService.deleteExam(examId, admin))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void teacher_cannot_list_exams() {
    assertThatThrownBy(() -> examService.listExamsForAssignment(assignmentId, teacher))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void admin_can_list_exams_for_assignment() {
    when(examRepository.findByAssignmentId(assignmentId))
        .thenReturn(
            List.of(
                JExam.builder()
                    .id(UUID.randomUUID())
                    .assignmentId(assignmentId)
                    .type(ExamType.REGULAR.name())
                    .build()));
    var result = examService.listExamsForAssignment(assignmentId, admin);
    assertThat(result).hasSize(1);
  }

  @Test
  void get_exam_returns_mapped_exam_when_admin() {
    var examId = UUID.randomUUID();
    var existing =
        JExam.builder()
            .id(examId)
            .assignmentId(assignmentId)
            .label("OOP")
            .coefficient(0.6)
            .type(ExamType.REGULAR.name())
            .build();
    when(examRepository.findById(examId)).thenReturn(Optional.of(existing));
    var result = examService.getExam(examId, admin);
    assertThat(result.label()).isEqualTo("OOP");
  }

  @Test
  void get_exam_throws_not_found_when_missing() {
    var examId = UUID.randomUUID();
    when(examRepository.findById(examId)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> examService.getExam(examId, admin))
        .isInstanceOf(ResponseStatusException.class);
  }
}
