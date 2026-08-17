package school.hei.students.service;

import static java.time.LocalDate.now;
import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.students.mapper.StudentGroupHistoryMapper;
import school.hei.students.mapper.StudentMapper;
import school.hei.students.model.Student;
import school.hei.students.model.StudentGroupHistory;
import school.hei.students.repository.StudentGroupHistoryRepository;
import school.hei.students.repository.StudentRepository;
import school.hei.students.repository.model.JStudent;
import school.hei.students.repository.model.JStudentGroupHistory;
import school.hei.students.service.exception.StudentNotFoundException;

class StudentServiceTest {
  private StudentRepository repository;
  private StudentMapper mapper;
  private StudentGroupHistoryRepository historyRepository;
  private StudentGroupHistoryMapper historyMapper;
  private StudentService subject;

  @BeforeEach
  void setUp() {
    repository = mock(StudentRepository.class);
    mapper = mock(StudentMapper.class);
    historyRepository = mock(StudentGroupHistoryRepository.class);
    historyMapper = mock(StudentGroupHistoryMapper.class);
    subject = new StudentService(repository, mapper, historyRepository, historyMapper);
  }

  @Test
  void getById_not_found_ko() {
    var id = randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> subject.getById(id)).isInstanceOf(StudentNotFoundException.class);
  }

  @Test
  void update_with_null_keeps_existing_ok() {
    var id = randomUUID();
    var jStudent = mock(JStudent.class);
    var existing =
        Student.builder()
            .id(id)
            .userId(randomUUID())
            .cohortId(randomUUID())
            .lastName("Rasoa")
            .firstName("Marie")
            .studentNumber("24001")
            .workStudy(false)
            .active(true)
            .build();
    when(repository.findById(id)).thenReturn(Optional.of(jStudent));
    when(mapper.toModel(jStudent)).thenReturn(existing);
    when(mapper.toEntity(any())).thenReturn(jStudent);
    when(repository.save(jStudent)).thenReturn(jStudent);
    var result = subject.update(id, null, null, null, null);
    assertThat(result).isEqualTo(existing);
  }

  @Test
  void deactivate_not_found_ko() {
    var id = randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> subject.deactivate(id)).isInstanceOf(StudentNotFoundException.class);
  }

  @Test
  void changeGroup_closes_active_history_ok() {
    var studentId = randomUUID();
    var jStudent = mock(JStudent.class);
    var existingStudent =
        Student.builder()
            .id(studentId)
            .userId(randomUUID())
            .cohortId(randomUUID())
            .lastName("Rasoa")
            .firstName("Marie")
            .studentNumber("24001")
            .workStudy(false)
            .active(true)
            .build();
    var activeHistory = mock(JStudentGroupHistory.class);
    var newGroupId = randomUUID();
    var changeDate = now();
    when(repository.findById(studentId)).thenReturn(Optional.of(jStudent));
    when(mapper.toModel(jStudent)).thenReturn(existingStudent);
    when(historyRepository.findByStudentIdAndEndDateIsNull(studentId))
        .thenReturn(List.of(activeHistory));
    when(historyRepository.save(activeHistory)).thenReturn(activeHistory);
    var savedHistory = mock(JStudentGroupHistory.class);
    var newHistoryModel =
        StudentGroupHistory.builder()
            .id(randomUUID())
            .studentId(studentId)
            .groupId(newGroupId)
            .startDate(changeDate)
            .build();
    when(historyMapper.toEntity(any())).thenReturn(savedHistory);
    when(historyRepository.save(savedHistory)).thenReturn(savedHistory);
    when(historyMapper.toModel(savedHistory)).thenReturn(newHistoryModel);
    var result = subject.changeGroup(studentId, newGroupId, changeDate);
    assertThat(result).isEqualTo(newHistoryModel);
    verify(activeHistory).setEndDate(changeDate);
  }

  @Test
  void getGroupHistory_ok() {
    var studentId = randomUUID();
    var jStudent = mock(JStudent.class);
    when(repository.findById(studentId)).thenReturn(Optional.of(jStudent));
    when(mapper.toModel(jStudent))
        .thenReturn(
            Student.builder()
                .id(studentId)
                .userId(randomUUID())
                .cohortId(randomUUID())
                .lastName("Rasoa")
                .firstName("Marie")
                .studentNumber("24001")
                .workStudy(false)
                .active(true)
                .build());
    var jHistories = List.of(mock(JStudentGroupHistory.class));
    var histories =
        List.of(
            StudentGroupHistory.builder()
                .id(randomUUID())
                .studentId(studentId)
                .groupId(randomUUID())
                .startDate(now())
                .build());
    when(historyRepository.findByStudentId(studentId)).thenReturn(jHistories);
    when(historyMapper.toModel(jHistories)).thenReturn(histories);
    var result = subject.getGroupHistory(studentId);
    assertThat(result).isEqualTo(histories);
  }
}
