package school.hei.students.service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.model.GradeActor;
import school.hei.students.repository.CourseAssignmentRepository;
import school.hei.students.repository.CourseRepository;
import school.hei.students.repository.ExamRepository;
import school.hei.students.repository.GradeRepository;
import school.hei.students.repository.StudentRepository;
import school.hei.students.repository.model.JCourseAssignment;
import school.hei.students.repository.model.JExam;
import school.hei.students.repository.model.JGrade;
import school.hei.students.repository.model.JStudent;
import school.hei.students.xlsx.GraduateRow;
import school.hei.students.xlsx.GraduateXlsxGenerator;

@Service
@AllArgsConstructor
public class GraduateExportService {
  private static final double PASSING_THRESHOLD = 10.0;
  private static final double REQUIRED_ANNUAL_CREDITS = 60.0;

  private final CohortService cohortService;
  private final StudentRepository studentRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;
  private final CourseRepository courseRepository;
  private final GraduateXlsxGenerator xlsxGenerator;

  public java.io.File export(UUID cohortId, GradeActor actor) {
    assertAdmin(actor);
    cohortService.getById(cohortId);
    var students = studentRepository.findByCohortId(cohortId);
    var graduates = new ArrayList<GraduateRow>();
    for (var student : students) {
      var summary = computeSummary(student);
      if (isGraduate(summary)) {
        graduates.add(
            GraduateRow.builder()
                .studentNumber(student.getStudentNumber())
                .lastName(student.getLastName())
                .firstName(student.getFirstName())
                .overallAverage(summary.overallAverage)
                .build());
      }
    }
    graduates.sort(Comparator.comparing(GraduateRow::overallAverage).reversed());
    return xlsxGenerator.generate(rank(graduates));
  }

  private List<GraduateRow> rank(List<GraduateRow> sortedGraduates) {
    var ranked = new ArrayList<GraduateRow>();
    var rank = 1;
    for (var graduate : sortedGraduates) {
      ranked.add(
          GraduateRow.builder()
              .rank(rank++)
              .studentNumber(graduate.studentNumber())
              .lastName(graduate.lastName())
              .firstName(graduate.firstName())
              .overallAverage(graduate.overallAverage())
              .build());
    }
    return ranked;
  }

  private boolean isGraduate(StudentAcademicSummary summary) {
    return summary.validatedCredits == REQUIRED_ANNUAL_CREDITS
        && summary.overallAverage != null
        && summary.overallAverage >= PASSING_THRESHOLD;
  }

  private StudentAcademicSummary computeSummary(JStudent student) {
    var studentGrades = gradeRepository.findByStudentId(student.getId());
    Map<UUID, double[]> weightedSumByCourse = new HashMap<>();
    Map<UUID, UUID> courseIdByAssignmentId = new HashMap<>();
    Map<UUID, UUID> assignmentIdByExamId = new HashMap<>();
    for (var grade : studentGrades) {
      var assignmentId = assignmentIdOf(grade, assignmentIdByExamId);
      if (assignmentId == null) {
        continue;
      }
      var courseId = courseIdOf(assignmentId, courseIdByAssignmentId);
      if (courseId == null) {
        continue;
      }
      var exam = examRepository.findById(grade.getExamId()).orElse(null);
      var coefficient = exam == null || exam.getCoefficient() == null ? 1.0 : exam.getCoefficient();
      var acc = weightedSumByCourse.computeIfAbsent(courseId, c -> new double[2]);
      acc[0] += grade.getValue() * coefficient;
      acc[1] += coefficient;
    }

    double creditWeightedSum = 0;
    double totalCredits = 0;
    double validatedCredits = 0;
    for (var entry : weightedSumByCourse.entrySet()) {
      var course = courseRepository.findById(entry.getKey()).orElse(null);
      if (course == null || course.getCredits() == null) {
        continue;
      }
      var acc = entry.getValue();
      var courseAverage = acc[1] == 0 ? 0 : acc[0] / acc[1];
      creditWeightedSum += courseAverage * course.getCredits();
      totalCredits += course.getCredits();
      if (courseAverage >= PASSING_THRESHOLD) {
        validatedCredits += course.getCredits();
      }
    }

    var overallAverage = totalCredits == 0 ? null : creditWeightedSum / totalCredits;
    return new StudentAcademicSummary(totalCredits, validatedCredits, overallAverage);
  }

  private UUID assignmentIdOf(JGrade grade, Map<UUID, UUID> cache) {
    if (cache.containsKey(grade.getExamId())) {
      return cache.get(grade.getExamId());
    }
    JExam exam = examRepository.findById(grade.getExamId()).orElse(null);
    var assignmentId = exam == null ? null : exam.getAssignmentId();
    cache.put(grade.getExamId(), assignmentId);
    return assignmentId;
  }

  private UUID courseIdOf(UUID assignmentId, Map<UUID, UUID> cache) {
    if (cache.containsKey(assignmentId)) {
      return cache.get(assignmentId);
    }
    JCourseAssignment assignment = courseAssignmentRepository.findById(assignmentId).orElse(null);
    var courseId = assignment == null ? null : assignment.getCourseId();
    cache.put(assignmentId, courseId);
    return courseId;
  }

  private void assertAdmin(GradeActor actor) {
    if (!actor.isAdmin()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can export graduates");
    }
  }

  private record StudentAcademicSummary(
      double totalCredits, double validatedCredits, Double overallAverage) {}
}
