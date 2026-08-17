package school.hei.students.service;

import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpStatus.CONFLICT;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.mapper.GroupMapper;
import school.hei.students.model.Group;
import school.hei.students.repository.CourseAssignmentTeachingRepository;
import school.hei.students.repository.GroupRepository;
import school.hei.students.repository.StudentGroupHistoryRepository;
import school.hei.students.service.exception.GroupNotFoundException;

@Service
@AllArgsConstructor
public class GroupService {
  private final GroupRepository repository;
  private final GroupMapper mapper;
  private final StudentGroupHistoryRepository studentGroupHistoryRepository;
  private final CourseAssignmentTeachingRepository courseAssignmentTeachingRepository;

  public List<Group> getAll(UUID cohortId, UUID trackId) {
    if (cohortId != null) {
      return mapper.toModel(repository.findByCohortId(cohortId));
    }
    if (trackId != null) {
      return mapper.toModel(repository.findByTrackId(trackId));
    }
    return mapper.toModel(repository.findAll());
  }

  public Group getById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new GroupNotFoundException(id)));
  }

  public Group create(String code, UUID trackId, UUID cohortId) {
    var toSave =
        Group.builder().id(randomUUID()).code(code).trackId(trackId).cohortId(cohortId).build();
    return mapper.toModel(repository.save(mapper.toEntity(toSave)));
  }

  public Group updateCode(UUID id, String code) {
    var existing = getById(id);
    var updated =
        Group.builder()
            .id(existing.id())
            .code(code != null ? code : existing.code())
            .trackId(existing.trackId())
            .cohortId(existing.cohortId())
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(updated)));
  }

  public void delete(UUID id) {
    getById(id);
    var hasHistory = !studentGroupHistoryRepository.findByGroupId(id).isEmpty();
    var hasTeaching = !courseAssignmentTeachingRepository.findByGroupId(id).isEmpty();
    if (hasHistory || hasTeaching) {
      throw new ResponseStatusException(
          CONFLICT, "Student history or teaching assignments are linked to this group");
    }
    repository.deleteById(id);
  }
}
