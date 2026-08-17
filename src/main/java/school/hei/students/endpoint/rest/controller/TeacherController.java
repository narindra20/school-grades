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
import school.hei.students.endpoint.rest.controller.dto.TeacherCreateRequest;
import school.hei.students.model.Teacher;
import school.hei.students.service.TeacherService;

@RestController
@RequestMapping("/admin/teachers")
@AllArgsConstructor
public class TeacherController {
  private final TeacherService service;

  @GetMapping
  public List<Teacher> getAll() {
    return service.getAll();
  }

  @GetMapping("/{teacherId}")
  public Teacher getById(@PathVariable UUID teacherId) {
    return service.getById(teacherId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Teacher create(@RequestBody TeacherCreateRequest request) {
    return service.create(request.userId(), request.lastName(), request.firstName());
  }

  @PutMapping("/{teacherId}")
  public Teacher update(@PathVariable UUID teacherId, @RequestBody TeacherCreateRequest request) {
    return service.update(teacherId, request.lastName(), request.firstName());
  }

  @DeleteMapping("/{teacherId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivate(@PathVariable UUID teacherId) {
    service.deactivate(teacherId);
  }
}
