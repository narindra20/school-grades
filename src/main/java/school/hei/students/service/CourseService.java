package school.hei.students.service;

import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpStatus.CONFLICT;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.mapper.CourseMapper;
import school.hei.students.model.Course;
import school.hei.students.repository.CourseAssignmentRepository;
import school.hei.students.repository.CourseRepository;
import school.hei.students.service.exception.CourseNotFoundException;

@Service
@AllArgsConstructor
public class CourseService {
  private final CourseRepository repository;
  private final CourseMapper mapper;
  private final CourseAssignmentRepository courseAssignmentRepository;

  public List<Course> getAll() {
    return mapper.toModel(repository.findAll());
  }

  public Course getById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new CourseNotFoundException(id)));
  }

  public Course create(String code, String title, Integer credits, String level, String semester) {
    var toSave =
        Course.builder()
            .id(randomUUID())
            .code(code)
            .title(title)
            .credits(credits)
            .level(level)
            .semester(semester)
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(toSave)));
  }

  public Course update(
      UUID id, String code, String title, Integer credits, String level, String semester) {
    var existing = getById(id);
    var updated =
        Course.builder()
            .id(existing.id())
            .code(code != null ? code : existing.code())
            .title(title != null ? title : existing.title())
            .credits(credits != null ? credits : existing.credits())
            .level(level != null ? level : existing.level())
            .semester(semester != null ? semester : existing.semester())
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(updated)));
  }

  public void delete(UUID id) {
    getById(id);
    if (!courseAssignmentRepository.findByCourseId(id).isEmpty()) {
      throw new ResponseStatusException(CONFLICT, "Course assignments are linked to this course");
    }
    repository.deleteById(id);
  }
}
