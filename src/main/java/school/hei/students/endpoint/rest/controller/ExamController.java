package school.hei.students.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.students.model.Exam;
import school.hei.students.model.ExamCreateRequest;
import school.hei.students.model.ExamUpdateRequest;
import school.hei.students.service.ExamService;
import school.hei.students.service.GradeActorResolver;

@RestController
@AllArgsConstructor
public class ExamController {
  private final ExamService examService;
  private final GradeActorResolver actorResolver;

  @GetMapping("/admin/course-assignments/{assignmentId}/exams")
  public List<Exam> listExams(@PathVariable UUID assignmentId, Authentication authentication) {
    return examService.listExamsForAssignment(assignmentId, actorResolver.resolve(authentication));
  }

  @PostMapping("/admin/course-assignments/{assignmentId}/exams")
  @ResponseStatus(HttpStatus.CREATED)
  public Exam createExam(
      @PathVariable UUID assignmentId,
      @RequestBody ExamCreateRequest request,
      Authentication authentication) {
    return examService.createExam(assignmentId, request, actorResolver.resolve(authentication));
  }

  @GetMapping("/admin/exams/{examId}")
  public Exam getExam(@PathVariable UUID examId, Authentication authentication) {
    return examService.getExam(examId, actorResolver.resolve(authentication));
  }

  @PutMapping("/admin/exams/{examId}")
  public Exam updateExam(
      @PathVariable UUID examId,
      @RequestBody ExamUpdateRequest request,
      Authentication authentication) {
    return examService.updateExam(examId, request, actorResolver.resolve(authentication));
  }

  @DeleteMapping("/admin/exams/{examId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteExam(@PathVariable UUID examId, Authentication authentication) {
    examService.deleteExam(examId, actorResolver.resolve(authentication));
  }
}
