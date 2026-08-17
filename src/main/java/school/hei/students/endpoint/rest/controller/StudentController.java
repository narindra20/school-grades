package school.hei.students.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.students.endpoint.rest.controller.dto.ChangeGroupRequest;
import school.hei.students.endpoint.rest.controller.dto.StudentCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.StudentUpdateRequest;
import school.hei.students.model.Student;
import school.hei.students.model.StudentGroupHistory;
import school.hei.students.service.StudentService;

@RestController
@RequestMapping("/admin/students")
@AllArgsConstructor
public class StudentController {
  private final StudentService service;

  @GetMapping
  public List<Student> getAll() {
    return service.getAll();
  }

  @GetMapping("/{studentId}")
  public Student getById(@PathVariable UUID studentId) {
    return service.getById(studentId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Student create(@RequestBody StudentCreateRequest request) {
    return service.create(
        request.userId(),
        request.cohortId(),
        request.lastName(),
        request.firstName(),
        request.studentNumber(),
        request.workStudy());
  }

  @PutMapping("/{studentId}")
  public Student update(@PathVariable UUID studentId, @RequestBody StudentUpdateRequest request) {
    return service.update(
        studentId,
        request.lastName(),
        request.firstName(),
        request.workStudy(),
        request.cohortId());
  }

  @DeleteMapping("/{studentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivate(@PathVariable UUID studentId) {
    service.deactivate(studentId);
  }

  @PostMapping("/{studentId}/change-group")
  public StudentGroupHistory changeGroup(
      @PathVariable UUID studentId, @RequestBody ChangeGroupRequest request) {
    return service.changeGroup(studentId, request.newGroupId(), request.changeDate());
  }

  @GetMapping("/{studentId}/group-history")
  public List<StudentGroupHistory> getGroupHistory(@PathVariable UUID studentId) {
    return service.getGroupHistory(studentId);
  }
}
