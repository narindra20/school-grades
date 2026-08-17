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
import school.hei.students.endpoint.rest.controller.dto.CourseCreateRequest;
import school.hei.students.model.Course;
import school.hei.students.service.CourseService;

@RestController
@RequestMapping("/admin/courses")
@AllArgsConstructor
public class CourseController {
  private final CourseService service;

  @GetMapping
  public List<Course> getAll() {
    return service.getAll();
  }

  @GetMapping("/{courseId}")
  public Course getById(@PathVariable UUID courseId) {
    return service.getById(courseId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Course create(@RequestBody CourseCreateRequest request) {
    return service.create(
        request.code(), request.title(), request.credits(), request.level(), request.semester());
  }

  @PutMapping("/{courseId}")
  public Course update(@PathVariable UUID courseId, @RequestBody CourseCreateRequest request) {
    return service.update(
        courseId,
        request.code(),
        request.title(),
        request.credits(),
        request.level(),
        request.semester());
  }

  @DeleteMapping("/{courseId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID courseId) {
    service.delete(courseId);
  }
}
