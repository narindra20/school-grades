package school.hei.students.service;

import static java.util.UUID.randomUUID;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.students.mapper.TeacherMapper;
import school.hei.students.model.Teacher;
import school.hei.students.repository.CourseAssignmentTeachingRepository;
import school.hei.students.repository.TeacherRepository;
import school.hei.students.service.exception.TeacherNotFoundException;

@Service
@AllArgsConstructor
public class TeacherService {
  private final TeacherRepository repository;
  private final TeacherMapper mapper;
  private final CourseAssignmentTeachingRepository courseAssignmentTeachingRepository;

  public List<Teacher> getAll() {
    return mapper.toModel(repository.findAll());
  }

  public Teacher getById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new TeacherNotFoundException(id)));
  }

  public Teacher create(UUID userId, String lastName, String firstName) {
    var toSave =
        Teacher.builder()
            .id(randomUUID())
            .userId(userId)
            .lastName(lastName)
            .firstName(firstName)
            .active(true)
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(toSave)));
  }

  public Teacher update(UUID id, String lastName, String firstName) {
    var existing = getById(id);
    var updated =
        Teacher.builder()
            .id(existing.id())
            .userId(existing.userId())
            .lastName(lastName != null ? lastName : existing.lastName())
            .firstName(firstName != null ? firstName : existing.firstName())
            .active(existing.active())
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(updated)));
  }

  public void deactivate(UUID id) {
    var entity = repository.findById(id).orElseThrow(() -> new TeacherNotFoundException(id));
    entity.setActive(false);
    repository.save(entity);
  }

  public boolean hasTeaching(UUID teacherId) {
    return !courseAssignmentTeachingRepository.findByTeacherId(teacherId).isEmpty();
  }
}
