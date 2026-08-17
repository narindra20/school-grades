package school.hei.students.service;

import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpStatus.CONFLICT;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.mapper.CohortMapper;
import school.hei.students.model.Cohort;
import school.hei.students.repository.CohortRepository;
import school.hei.students.repository.GroupRepository;
import school.hei.students.repository.StudentRepository;
import school.hei.students.service.exception.CohortNotFoundException;

@Service
@AllArgsConstructor
public class CohortService {
  private final CohortRepository repository;
  private final CohortMapper mapper;
  private final StudentRepository studentRepository;
  private final GroupRepository groupRepository;

  public List<Cohort> getAll() {
    return mapper.toModel(repository.findAll());
  }

  public Cohort getById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new CohortNotFoundException(id)));
  }

  public Cohort create(Integer entryYear) {
    var toSave = Cohort.builder().id(randomUUID()).entryYear(entryYear).build();
    return mapper.toModel(repository.save(mapper.toEntity(toSave)));
  }

  public Cohort update(UUID id, Integer entryYear) {
    var existing = getById(id);
    var updated =
        Cohort.builder()
            .id(existing.id())
            .entryYear(entryYear != null ? entryYear : existing.entryYear())
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(updated)));
  }

  public void delete(UUID id) {
    getById(id);
    var hasStudents = !studentRepository.findByCohortId(id).isEmpty();
    var hasGroups = !groupRepository.findByCohortId(id).isEmpty();
    if (hasStudents || hasGroups) {
      throw new ResponseStatusException(CONFLICT, "Students or groups are linked to this cohort");
    }
    repository.deleteById(id);
  }
}
