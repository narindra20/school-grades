package school.hei.students.service;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.mapper.CourseAssignmentMapper;
import school.hei.students.mapper.CourseAssignmentTeachingMapper;
import school.hei.students.model.CourseAssignment;
import school.hei.students.repository.CourseAssignmentRepository;
import school.hei.students.repository.CourseAssignmentTeachingRepository;
import school.hei.students.repository.ExamRepository;
import school.hei.students.repository.GradeRepository;
import school.hei.students.repository.StudentGroupHistoryRepository;
import school.hei.students.repository.model.JCourseAssignment;
import school.hei.students.repository.model.JCourseAssignmentTeaching;
import school.hei.students.repository.model.JExam;
import school.hei.students.repository.model.JGrade;
import school.hei.students.repository.model.JStudentGroupHistory;
import school.hei.students.service.exception.CourseAssignmentNotFoundException;
import school.hei.students.service.exception.CourseAssignmentTeachingNotFoundException;

class CourseAssignmentServiceTest {
  private CourseAssignmentRepository repository;
  private CourseAssignmentMapper mapper;
  private CourseAssignmentTeachingRepository teachingRepository;
  private CourseAssignmentTeachingMapper teachingMapper;
  private ExamRepository examRepository;
  private GradeRepository gradeRepository;
  private StudentGroupHistoryRepository studentGroupHistoryRepository;
  private CourseAssignmentService subject;

  @BeforeEach
  void setUp() {
    repository = mock(CourseAssignmentRepository.class);
    mapper = mock(CourseAssignmentMapper.class);
    teachingRepository = mock(CourseAssignmentTeachingRepository.class);
    teachingMapper = mock(CourseAssignmentTeachingMapper.class);
    examRepository = mock(ExamRepository.class);
    gradeRepository = mock(GradeRepository.class);
    studentGroupHistoryRepository = mock(StudentGroupHistoryRepository.class);
    subject =
        new CourseAssignmentService(
            repository,
            mapper,
            teachingRepository,
            teachingMapper,
            examRepository,
            gradeRepository,
            studentGroupHistoryRepository);
  }

  @Test
  void getAll_with_academicYear_filter_ok() {
    when(repository.findByAcademicYear("2025-2026")).thenReturn(List.of());
    when(mapper.toModel(List.<JCourseAssignment>of())).thenReturn(List.of());
    var result = subject.getAll("2025-2026");
    assertThat(result).isEmpty();
  }

  @Test
  void getById_not_found_ko() {
    var id = randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> subject.getById(id))
        .isInstanceOf(CourseAssignmentNotFoundException.class);
  }

  @Test
  void delete_with_linked_teaching_ko() {
    var id = randomUUID();
    var jAssignment = mock(JCourseAssignment.class);
    when(repository.findById(id)).thenReturn(Optional.of(jAssignment));
    when(mapper.toModel(jAssignment))
        .thenReturn(
            CourseAssignment.builder()
                .id(id)
                .courseId(randomUUID())
                .academicYear("2025-2026")
                .build());
    when(teachingRepository.findByAssignmentId(id))
        .thenReturn(List.of(mock(JCourseAssignmentTeaching.class)));
    assertThatThrownBy(() -> subject.delete(id)).isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void removeTeaching_not_found_ko() {
    var id = randomUUID();
    when(teachingRepository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> subject.removeTeaching(id))
        .isInstanceOf(CourseAssignmentTeachingNotFoundException.class);
  }

  @Test
  void removeTeaching_with_existing_grades_ko() {
    var teachingId = randomUUID();
    var assignmentId = randomUUID();
    var groupId = randomUUID();
    var examId = randomUUID();
    var studentId = randomUUID();
    var jTeaching = mock(JCourseAssignmentTeaching.class);
    when(jTeaching.getAssignmentId()).thenReturn(assignmentId);
    when(jTeaching.getGroupId()).thenReturn(groupId);
    when(teachingRepository.findById(teachingId)).thenReturn(Optional.of(jTeaching));
    var jHistory = mock(JStudentGroupHistory.class);
    when(jHistory.getStudentId()).thenReturn(studentId);
    when(studentGroupHistoryRepository.findByGroupId(groupId)).thenReturn(List.of(jHistory));
    var jExam = mock(JExam.class);
    when(jExam.getId()).thenReturn(examId);
    when(examRepository.findByAssignmentId(assignmentId)).thenReturn(List.of(jExam));
    var jGrade = mock(JGrade.class);
    when(jGrade.getStudentId()).thenReturn(studentId);
    when(gradeRepository.findByExamId(examId)).thenReturn(List.of(jGrade));
    assertThatThrownBy(() -> subject.removeTeaching(teachingId))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void removeTeaching_without_grades_ok() {
    var teachingId = randomUUID();
    var assignmentId = randomUUID();
    var groupId = randomUUID();
    var examId = randomUUID();
    var jTeaching = mock(JCourseAssignmentTeaching.class);
    when(jTeaching.getAssignmentId()).thenReturn(assignmentId);
    when(jTeaching.getGroupId()).thenReturn(groupId);
    when(teachingRepository.findById(teachingId)).thenReturn(Optional.of(jTeaching));
    when(studentGroupHistoryRepository.findByGroupId(groupId)).thenReturn(List.of());
    var jExam = mock(JExam.class);
    when(jExam.getId()).thenReturn(examId);
    when(examRepository.findByAssignmentId(assignmentId)).thenReturn(List.of(jExam));
    when(gradeRepository.findByExamId(examId)).thenReturn(List.of());
    subject.removeTeaching(teachingId);
    verify(teachingRepository).deleteById(teachingId);
  }
}
