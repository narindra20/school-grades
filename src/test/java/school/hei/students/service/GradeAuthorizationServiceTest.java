package school.hei.students.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.model.GradeActor;
import school.hei.students.model.Role;
import school.hei.students.repository.CourseAssignmentTeachingRepository;
import school.hei.students.repository.ExamRepository;
import school.hei.students.repository.StudentGroupHistoryRepository;
import school.hei.students.repository.model.JCourseAssignmentTeaching;
import school.hei.students.repository.model.JExam;
import school.hei.students.repository.model.JStudentGroupHistory;

@ExtendWith(MockitoExtension.class)
class GradeAuthorizationServiceTest {
  @Mock private StudentGroupHistoryRepository studentGroupHistoryRepository;
  @Mock private CourseAssignmentTeachingRepository courseAssignmentTeachingRepository;
  @Mock private ExamRepository examRepository;
  private GradeAuthorizationService authorizationService;
  private UUID studentId;
  private UUID teacherId;
  private UUID groupId;

  @BeforeEach
  void setUp() {
    authorizationService =
        new GradeAuthorizationService(
            studentGroupHistoryRepository, courseAssignmentTeachingRepository, examRepository);
    studentId = UUID.randomUUID();
    teacherId = UUID.randomUUID();
    groupId = UUID.randomUUID();
  }

  @Test
  void student_can_read_own_grades() {
    var actor = GradeActor.builder().role(Role.STUDENT).studentId(studentId).build();
    assertThatCode(() -> authorizationService.assertCanReadStudentGrades(actor, studentId))
        .doesNotThrowAnyException();
  }

  @Test
  void student_cannot_read_others_grades() {
    var actor = GradeActor.builder().role(Role.STUDENT).studentId(UUID.randomUUID()).build();
    assertThatThrownBy(() -> authorizationService.assertCanReadStudentGrades(actor, studentId))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void teacher_can_read_grades_of_student_they_taught() {
    var actor = GradeActor.builder().role(Role.TEACHER).teacherId(teacherId).build();
    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(List.of(JStudentGroupHistory.builder().groupId(groupId).build()));
    when(courseAssignmentTeachingRepository.findByTeacherId(teacherId))
        .thenReturn(List.of(JCourseAssignmentTeaching.builder().groupId(groupId).build()));
    assertThatCode(() -> authorizationService.assertCanReadStudentGrades(actor, studentId))
        .doesNotThrowAnyException();
  }

  @Test
  void teacher_cannot_read_grades_of_student_they_never_taught() {
    var actor = GradeActor.builder().role(Role.TEACHER).teacherId(teacherId).build();
    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(List.of(JStudentGroupHistory.builder().groupId(groupId).build()));
    when(courseAssignmentTeachingRepository.findByTeacherId(teacherId))
        .thenReturn(
            List.of(JCourseAssignmentTeaching.builder().groupId(UUID.randomUUID()).build()));
    assertThatThrownBy(() -> authorizationService.assertCanReadStudentGrades(actor, studentId))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void teacher_can_write_grade_when_owning_exam_assignment_and_group() {
    var examId = UUID.randomUUID();
    var assignmentId = UUID.randomUUID();
    var actor = GradeActor.builder().role(Role.TEACHER).teacherId(teacherId).build();
    when(examRepository.findById(examId))
        .thenReturn(Optional.of(JExam.builder().id(examId).assignmentId(assignmentId).build()));
    when(courseAssignmentTeachingRepository.findByAssignmentId(assignmentId))
        .thenReturn(
            List.of(
                JCourseAssignmentTeaching.builder().teacherId(teacherId).groupId(groupId).build()));
    when(studentGroupHistoryRepository.findByStudentId(studentId))
        .thenReturn(List.of(JStudentGroupHistory.builder().groupId(groupId).build()));
    assertThatCode(() -> authorizationService.assertCanWriteGradeForExam(actor, examId, studentId))
        .doesNotThrowAnyException();
  }

  @Test
  void teacher_cannot_write_grade_for_exam_of_another_teacher() {
    var examId = UUID.randomUUID();
    var assignmentId = UUID.randomUUID();
    var actor = GradeActor.builder().role(Role.TEACHER).teacherId(teacherId).build();
    when(examRepository.findById(examId))
        .thenReturn(Optional.of(JExam.builder().id(examId).assignmentId(assignmentId).build()));
    when(courseAssignmentTeachingRepository.findByAssignmentId(assignmentId))
        .thenReturn(
            List.of(
                JCourseAssignmentTeaching.builder()
                    .teacherId(UUID.randomUUID())
                    .groupId(groupId)
                    .build()));
    assertThatThrownBy(
            () -> authorizationService.assertCanWriteGradeForExam(actor, examId, studentId))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void admin_can_always_read() {
    var actor = GradeActor.builder().role(Role.ADMIN).build();
    assertThatCode(() -> authorizationService.assertCanReadStudentGrades(actor, studentId))
        .doesNotThrowAnyException();
  }
}
