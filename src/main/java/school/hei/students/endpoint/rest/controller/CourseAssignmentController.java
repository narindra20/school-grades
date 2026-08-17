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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.students.endpoint.rest.controller.dto.CourseAssignmentCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.TeachingAssignRequest;
import school.hei.students.model.CourseAssignment;
import school.hei.students.model.CourseAssignmentTeaching;
import school.hei.students.service.CourseAssignmentService;

@RestController
@RequestMapping("/admin/course-assignments")
@AllArgsConstructor
public class CourseAssignmentController {
  private final CourseAssignmentService service;

  @GetMapping
  public List<CourseAssignment> getAll(@RequestParam(required = false) String academicYear) {
    return service.getAll(academicYear);
  }

  @GetMapping("/{assignmentId}")
  public CourseAssignment getById(@PathVariable UUID assignmentId) {
    return service.getById(assignmentId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CourseAssignment create(@RequestBody CourseAssignmentCreateRequest request) {
    return service.create(request.courseId(), request.academicYear());
  }

  @PutMapping("/{assignmentId}")
  public CourseAssignment update(
      @PathVariable UUID assignmentId, @RequestBody CourseAssignmentCreateRequest request) {
    return service.update(assignmentId, request.academicYear());
  }

  @DeleteMapping("/{assignmentId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID assignmentId) {
    service.delete(assignmentId);
  }

  @GetMapping("/{assignmentId}/teaching")
  public List<CourseAssignmentTeaching> getTeaching(@PathVariable UUID assignmentId) {
    return service.getTeaching(assignmentId);
  }

  @PostMapping("/{assignmentId}/teaching")
  @ResponseStatus(HttpStatus.CREATED)
  public CourseAssignmentTeaching assignTeaching(
      @PathVariable UUID assignmentId, @RequestBody TeachingAssignRequest request) {
    return service.assignTeaching(assignmentId, request.teacherId(), request.groupId());
  }
}
