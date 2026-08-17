package school.hei.students.endpoint.rest.controller;

import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.students.service.CourseAssignmentService;

@RestController
@RequestMapping("/admin/course-assignment-teaching")
@AllArgsConstructor
public class CourseAssignmentTeachingController {
  private final CourseAssignmentService service;

  @DeleteMapping("/{teachingId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(@PathVariable UUID teachingId) {
    service.removeTeaching(teachingId);
  }
}
