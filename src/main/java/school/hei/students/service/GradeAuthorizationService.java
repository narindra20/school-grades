package school.hei.students.service;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.model.GradeActor;
import school.hei.students.repository.CourseAssignmentTeachingRepository;
import school.hei.students.repository.ExamRepository;
import school.hei.students.repository.StudentGroupHistoryRepository;

@Service
@AllArgsConstructor
public class GradeAuthorizationService {
  private static final String ACCESS_DENIED_MESSAGE =
      "The teacher did not teach this student on this course";

  private final StudentGroupHistoryRepository studentGroupHistoryRepository;
  private final CourseAssignmentTeachingRepository courseAssignmentTeachingRepository;
  private final ExamRepository examRepository;

  public void assertCanReadStudentGrades(GradeActor actor, UUID studentId) {
    if (actor.isAdmin()) {
      return;
    }
    if (actor.isStudent() && studentId.equals(actor.studentId())) {
      return;
    }
    if (actor.isTeacher() && teacherTaughtStudent(actor.teacherId(), studentId)) {
      return;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCESS_DENIED_MESSAGE);
  }

  public void assertCanWriteGradeForExam(GradeActor actor, UUID examId, UUID studentId) {
    if (actor.isAdmin()) {
      return;
    }
    if (actor.isTeacher() && teacherOwnsExamForStudent(actor.teacherId(), examId, studentId)) {
      return;
    }
    throw new ResponseStatusException(HttpStatus.FORBIDDEN, ACCESS_DENIED_MESSAGE);
  }

  private boolean teacherTaughtStudent(UUID teacherId, UUID studentId) {
    var studentGroupIds = studentGroupIdsOf(studentId);
    var teacherGroupIds =
        courseAssignmentTeachingRepository.findByTeacherId(teacherId).stream()
            .map(t -> t.getGroupId())
            .collect(Collectors.toSet());
    return !Collections.disjoint(studentGroupIds, teacherGroupIds);
  }

  private boolean teacherOwnsExamForStudent(UUID teacherId, UUID examId, UUID studentId) {
    var exam =
        examRepository
            .findById(examId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));
    var teacherGroupIds =
        courseAssignmentTeachingRepository.findByAssignmentId(exam.getAssignmentId()).stream()
            .filter(t -> teacherId.equals(t.getTeacherId()))
            .map(t -> t.getGroupId())
            .collect(Collectors.toSet());
    if (teacherGroupIds.isEmpty()) {
      return false;
    }
    return !Collections.disjoint(teacherGroupIds, studentGroupIdsOf(studentId));
  }

  private Set<UUID> studentGroupIdsOf(UUID studentId) {
    return studentGroupHistoryRepository.findByStudentId(studentId).stream()
        .map(h -> h.getGroupId())
        .collect(Collectors.toSet());
  }
}
