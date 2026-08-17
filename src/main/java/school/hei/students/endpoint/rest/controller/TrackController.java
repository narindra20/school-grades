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
import school.hei.students.endpoint.rest.controller.dto.TrackCreateRequest;
import school.hei.students.model.Course;
import school.hei.students.model.Track;
import school.hei.students.service.TrackService;

@RestController
@RequestMapping("/admin/tracks")
@AllArgsConstructor
public class TrackController {
  private final TrackService service;

  @GetMapping
  public List<Track> getAll() {
    return service.getAll();
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Track create(@RequestBody TrackCreateRequest request) {
    return service.create(request.name());
  }

  @PutMapping("/{trackId}")
  public Track update(@PathVariable UUID trackId, @RequestBody TrackCreateRequest request) {
    return service.update(trackId, request.name());
  }

  @DeleteMapping("/{trackId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID trackId) {
    service.delete(trackId);
  }

  @GetMapping("/{trackId}/courses")
  public List<Course> getCourses(@PathVariable UUID trackId) {
    return service.getCourses(trackId);
  }

  @PostMapping("/{trackId}/courses/{courseId}")
  @ResponseStatus(HttpStatus.CREATED)
  public void linkCourse(@PathVariable UUID trackId, @PathVariable UUID courseId) {
    service.linkCourse(trackId, courseId);
  }

  @DeleteMapping("/{trackId}/courses/{courseId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void unlinkCourse(@PathVariable UUID trackId, @PathVariable UUID courseId) {
    service.unlinkCourse(trackId, courseId);
  }
}
