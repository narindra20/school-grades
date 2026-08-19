package school.hei.students.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.model.GradeActor;
import school.hei.students.model.Role;
import school.hei.students.repository.CourseAssignmentRepository;
import school.hei.students.repository.CourseRepository;
import school.hei.students.repository.ExamRepository;
import school.hei.students.repository.GradeRepository;
import school.hei.students.repository.StudentRepository;
import school.hei.students.repository.model.JCourse;
import school.hei.students.repository.model.JCourseAssignment;
import school.hei.students.repository.model.JExam;
import school.hei.students.repository.model.JGrade;
import school.hei.students.repository.model.JStudent;
import school.hei.students.xlsx.GraduateXlsxGenerator;

@ExtendWith(MockitoExtension.class)
class GraduateExportServiceTest {
  @Mock private CohortService cohortService;
  @Mock private StudentRepository studentRepository;
  @Mock private CourseAssignmentRepository courseAssignmentRepository;
  @Mock private ExamRepository examRepository;
  @Mock private GradeRepository gradeRepository;
  @Mock private CourseRepository courseRepository;
  private GraduateExportService graduateExportService;
  private UUID cohortId;
  private GradeActor admin;
  private GradeActor teacher;

  @BeforeEach
  void setUp() {
    graduateExportService =
        new GraduateExportService(
            cohortService,
            studentRepository,
            courseAssignmentRepository,
            examRepository,
            gradeRepository,
            courseRepository,
            new GraduateXlsxGenerator());
    cohortId = UUID.randomUUID();
    admin = GradeActor.builder().role(Role.ADMIN).build();
    teacher = GradeActor.builder().role(Role.TEACHER).teacherId(UUID.randomUUID()).build();
  }

  @Test
  void non_admin_cannot_export_graduates() {
    assertThatThrownBy(() -> graduateExportService.export(cohortId, teacher))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void student_who_validated_everything_is_a_graduate() {
    var student = student("25001", "Rakoto", "Fitia");
    when(studentRepository.findByCohortId(cohortId)).thenReturn(List.of(student));

    var assignmentId = UUID.randomUUID();
    var courseId = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var exam = JExam.builder().id(examId).assignmentId(assignmentId).coefficient(1.0).build();
    var assignment =
        JCourseAssignment.builder()
            .id(assignmentId)
            .courseId(courseId)
            .academicYear("2025-2026")
            .build();
    var course =
        JCourse.builder().id(courseId).code("prog1").title("Programming 1").credits(60).build();
    var grade =
        JGrade.builder()
            .id(UUID.randomUUID())
            .studentId(student.getId())
            .examId(examId)
            .value(15.0)
            .build();

    when(gradeRepository.findByStudentId(student.getId())).thenReturn(List.of(grade));
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(courseAssignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

    var file = graduateExportService.export(cohortId, admin);

    var rows = readRows(file);
    assertThat(rows).hasSize(2);
    assertThat(rows.get(1)[0]).isEqualTo("1");
    assertThat(rows.get(1)[1]).isEqualTo("25001");
    assertThat(rows.get(1)[4]).isEqualTo("15.0");
  }

  @Test
  void student_with_failing_average_is_not_a_graduate() {
    var student = student("25002", "Rabe", "Hery");
    when(studentRepository.findByCohortId(cohortId)).thenReturn(List.of(student));

    var assignmentId = UUID.randomUUID();
    var courseId = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var exam = JExam.builder().id(examId).assignmentId(assignmentId).coefficient(1.0).build();
    var assignment =
        JCourseAssignment.builder()
            .id(assignmentId)
            .courseId(courseId)
            .academicYear("2025-2026")
            .build();
    var course =
        JCourse.builder().id(courseId).code("prog1").title("Programming 1").credits(60).build();
    var grade =
        JGrade.builder()
            .id(UUID.randomUUID())
            .studentId(student.getId())
            .examId(examId)
            .value(5.0)
            .build();

    when(gradeRepository.findByStudentId(student.getId())).thenReturn(List.of(grade));
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(courseAssignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

    var file = graduateExportService.export(cohortId, admin);

    assertThat(readRows(file)).hasSize(1);
  }

  @Test
  void student_with_no_grades_is_not_a_graduate() {
    var student = student("25003", "Andria", "Nomena");
    when(studentRepository.findByCohortId(cohortId)).thenReturn(List.of(student));
    when(gradeRepository.findByStudentId(student.getId())).thenReturn(List.of());

    var file = graduateExportService.export(cohortId, admin);

    assertThat(readRows(file)).hasSize(1);
  }

  @Test
  void graduates_are_ranked_by_descending_average() {
    var studentLow = student("25004", "Low", "Score");
    var studentHigh = student("25005", "High", "Score");
    when(studentRepository.findByCohortId(cohortId)).thenReturn(List.of(studentLow, studentHigh));

    var assignmentId = UUID.randomUUID();
    var courseId = UUID.randomUUID();
    var course =
        JCourse.builder().id(courseId).code("prog1").title("Programming 1").credits(60).build();
    var assignment =
        JCourseAssignment.builder()
            .id(assignmentId)
            .courseId(courseId)
            .academicYear("2025-2026")
            .build();
    when(courseAssignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

    var examLowId = UUID.randomUUID();
    var examHighId = UUID.randomUUID();
    when(examRepository.findById(examLowId))
        .thenReturn(
            Optional.of(
                JExam.builder().id(examLowId).assignmentId(assignmentId).coefficient(1.0).build()));
    when(examRepository.findById(examHighId))
        .thenReturn(
            Optional.of(
                JExam.builder()
                    .id(examHighId)
                    .assignmentId(assignmentId)
                    .coefficient(1.0)
                    .build()));

    when(gradeRepository.findByStudentId(studentLow.getId()))
        .thenReturn(
            List.of(
                JGrade.builder()
                    .id(UUID.randomUUID())
                    .studentId(studentLow.getId())
                    .examId(examLowId)
                    .value(11.0)
                    .build()));
    when(gradeRepository.findByStudentId(studentHigh.getId()))
        .thenReturn(
            List.of(
                JGrade.builder()
                    .id(UUID.randomUUID())
                    .studentId(studentHigh.getId())
                    .examId(examHighId)
                    .value(18.0)
                    .build()));

    var file = graduateExportService.export(cohortId, admin);

    var rows = readRows(file);
    assertThat(rows).hasSize(3);
    assertThat(rows.get(1)[1]).isEqualTo("25005");
    assertThat(rows.get(1)[0]).isEqualTo("1");
    assertThat(rows.get(2)[1]).isEqualTo("25004");
    assertThat(rows.get(2)[0]).isEqualTo("2");
  }

  @Test
  void student_with_less_than_60_validated_credits_is_not_a_graduate() {
    var student = student("25006", "Petit", "Credit");
    when(studentRepository.findByCohortId(cohortId)).thenReturn(List.of(student));

    var assignmentId = UUID.randomUUID();
    var courseId = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var exam = JExam.builder().id(examId).assignmentId(assignmentId).coefficient(1.0).build();
    var assignment =
        JCourseAssignment.builder()
            .id(assignmentId)
            .courseId(courseId)
            .academicYear("2025-2026")
            .build();
    var course =
        JCourse.builder().id(courseId).code("prog1").title("Programming 1").credits(30).build();
    var grade =
        JGrade.builder()
            .id(UUID.randomUUID())
            .studentId(student.getId())
            .examId(examId)
            .value(15.0)
            .build();

    when(gradeRepository.findByStudentId(student.getId())).thenReturn(List.of(grade));
    when(examRepository.findById(examId)).thenReturn(Optional.of(exam));
    when(courseAssignmentRepository.findById(assignmentId)).thenReturn(Optional.of(assignment));
    when(courseRepository.findById(courseId)).thenReturn(Optional.of(course));

    var file = graduateExportService.export(cohortId, admin);

    assertThat(readRows(file)).hasSize(1);
  }

  private JStudent student(String studentNumber, String lastName, String firstName) {
    return JStudent.builder()
        .id(UUID.randomUUID())
        .userId(UUID.randomUUID())
        .cohortId(cohortId)
        .studentNumber(studentNumber)
        .lastName(lastName)
        .firstName(firstName)
        .workStudy(false)
        .active(true)
        .build();
  }

  private List<String[]> readRows(java.io.File file) {
    try (var workbook = WorkbookFactory.create(file)) {
      var sheet = workbook.getSheetAt(0);
      var rows = new java.util.ArrayList<String[]>();
      for (var row : sheet) {
        var cells = new String[5];
        for (var i = 0; i < 5; i++) {
          var cell = row.getCell(i);
          cells[i] = cell == null ? "" : cellToString(i, cell);
        }
        rows.add(cells);
      }
      return rows;
    } catch (java.io.IOException e) {
      throw new RuntimeException(e);
    }
  }

  private String cellToString(int columnIndex, org.apache.poi.ss.usermodel.Cell cell) {
    if (columnIndex == 0 && cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
      return String.valueOf((long) cell.getNumericCellValue());
    }
    return cell.toString();
  }
}
