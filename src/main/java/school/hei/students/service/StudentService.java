package school.hei.students.service;

import static java.util.UUID.randomUUID;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import school.hei.students.mapper.StudentGroupHistoryMapper;
import school.hei.students.mapper.StudentMapper;
import school.hei.students.model.Student;
import school.hei.students.model.StudentGroupHistory;
import school.hei.students.repository.StudentGroupHistoryRepository;
import school.hei.students.repository.StudentRepository;
import school.hei.students.service.exception.StudentNotFoundException;

@Service
@AllArgsConstructor
public class StudentService {
  private final StudentRepository repository;
  private final StudentMapper mapper;
  private final StudentGroupHistoryRepository historyRepository;
  private final StudentGroupHistoryMapper historyMapper;

  public List<Student> getAll() {
    return mapper.toModel(repository.findAll());
  }

  public Student getById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new StudentNotFoundException(id)));
  }

  public Student create(
      UUID userId,
      UUID cohortId,
      String lastName,
      String firstName,
      String studentNumber,
      boolean workStudy) {
    var toSave =
        Student.builder()
            .id(randomUUID())
            .userId(userId)
            .cohortId(cohortId)
            .lastName(lastName)
            .firstName(firstName)
            .studentNumber(studentNumber)
            .workStudy(workStudy)
            .active(true)
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(toSave)));
  }

  public Student update(
      UUID id, String lastName, String firstName, Boolean workStudy, UUID cohortId) {
    var existing = getById(id);
    var updated =
        Student.builder()
            .id(existing.id())
            .userId(existing.userId())
            .cohortId(cohortId != null ? cohortId : existing.cohortId())
            .lastName(lastName != null ? lastName : existing.lastName())
            .firstName(firstName != null ? firstName : existing.firstName())
            .studentNumber(existing.studentNumber())
            .workStudy(workStudy != null ? workStudy : existing.workStudy())
            .active(existing.active())
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(updated)));
  }

  public void deactivate(UUID id) {
    var entity = repository.findById(id).orElseThrow(() -> new StudentNotFoundException(id));
    entity.setActive(false);
    repository.save(entity);
  }

  public StudentGroupHistory changeGroup(UUID studentId, UUID newGroupId, LocalDate changeDate) {
    getById(studentId);
    var activeHistories = historyRepository.findByStudentIdAndEndDateIsNull(studentId);
    for (var active : activeHistories) {
      active.setEndDate(changeDate);
      historyRepository.save(active);
    }
    var newHistory =
        StudentGroupHistory.builder()
            .id(randomUUID())
            .studentId(studentId)
            .groupId(newGroupId)
            .startDate(changeDate)
            .endDate(null)
            .build();
    return historyMapper.toModel(historyRepository.save(historyMapper.toEntity(newHistory)));
  }

  public List<StudentGroupHistory> getGroupHistory(UUID studentId) {
    getById(studentId);
    return historyMapper.toModel(historyRepository.findByStudentId(studentId));
  }
}
