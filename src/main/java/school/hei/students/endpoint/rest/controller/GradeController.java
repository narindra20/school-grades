package school.hei.students.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.students.endpoint.rest.controller.dto.GradeCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.GradeUpdateRequest;
import school.hei.students.model.Grade;
import school.hei.students.model.GradeHistory;
import school.hei.students.service.GradeActorResolver;
import school.hei.students.service.GradeService;

@RestController
@AllArgsConstructor
public class GradeController {
  private final GradeService gradeService;
  private final GradeActorResolver actorResolver;

  @GetMapping("/students/{studentId}/grades")
  public List<Grade> listGrades(@PathVariable UUID studentId, Authentication authentication) {
    return gradeService.listGradesForStudent(studentId, actorResolver.resolve(authentication));
  }

  @PostMapping("/students/{studentId}/grades")
  @ResponseStatus(HttpStatus.CREATED)
  public Grade createGrade(
      @PathVariable UUID studentId,
      @RequestBody GradeCreateRequest request,
      Authentication authentication) {
    return gradeService.createGrade(studentId, request, actorResolver.resolve(authentication));
  }

  @GetMapping("/grades/{gradeId}")
  public Grade getGrade(@PathVariable UUID gradeId, Authentication authentication) {
    return gradeService.getGrade(gradeId, actorResolver.resolve(authentication));
  }

  @PutMapping("/grades/{gradeId}")
  public Grade updateGrade(
      @PathVariable UUID gradeId,
      @RequestBody GradeUpdateRequest request,
      Authentication authentication) {
    return gradeService.updateGrade(gradeId, request, actorResolver.resolve(authentication));
  }

  @GetMapping("/grades/{gradeId}/history")
  public List<GradeHistory> getGradeHistory(
      @PathVariable UUID gradeId, Authentication authentication) {
    return gradeService.getGradeHistory(gradeId, actorResolver.resolve(authentication));
  }
}
