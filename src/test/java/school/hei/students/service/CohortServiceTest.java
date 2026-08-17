package school.hei.students.service;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.mapper.CohortMapper;
import school.hei.students.model.Cohort;
import school.hei.students.repository.CohortRepository;
import school.hei.students.repository.GroupRepository;
import school.hei.students.repository.StudentRepository;
import school.hei.students.repository.model.JCohort;
import school.hei.students.repository.model.JGroup;
import school.hei.students.repository.model.JStudent;
import school.hei.students.service.exception.CohortNotFoundException;

class CohortServiceTest {
  private CohortRepository repository;
  private CohortMapper mapper;
  private StudentRepository studentRepository;
  private GroupRepository groupRepository;
  private CohortService subject;

  @BeforeEach
  void setUp() {
    repository = mock(CohortRepository.class);
    mapper = mock(CohortMapper.class);
    studentRepository = mock(StudentRepository.class);
    groupRepository = mock(GroupRepository.class);
    subject = new CohortService(repository, mapper, studentRepository, groupRepository);
  }

  @Test
  void getById_not_found_ko() {
    var id = randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> subject.getById(id)).isInstanceOf(CohortNotFoundException.class);
  }

  @Test
  void update_with_null_keeps_existing_ok() {
    var id = randomUUID();
    var jCohort = mock(JCohort.class);
    var existing = Cohort.builder().id(id).entryYear(2024).build();
    when(repository.findById(id)).thenReturn(Optional.of(jCohort));
    when(mapper.toModel(jCohort)).thenReturn(existing);
    when(mapper.toEntity(any())).thenReturn(jCohort);
    when(repository.save(jCohort)).thenReturn(jCohort);
    var result = subject.update(id, null);
    assertThat(result).isEqualTo(existing);
  }

  @Test
  void delete_with_linked_students_ko() {
    var id = randomUUID();
    var jCohort = mock(JCohort.class);
    when(repository.findById(id)).thenReturn(Optional.of(jCohort));
    when(mapper.toModel(jCohort)).thenReturn(Cohort.builder().id(id).entryYear(2024).build());
    when(studentRepository.findByCohortId(id)).thenReturn(List.of(mock(JStudent.class)));
    when(groupRepository.findByCohortId(id)).thenReturn(List.of());
    assertThatThrownBy(() -> subject.delete(id)).isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void delete_with_linked_groups_ko() {
    var id = randomUUID();
    var jCohort = mock(JCohort.class);
    when(repository.findById(id)).thenReturn(Optional.of(jCohort));
    when(mapper.toModel(jCohort)).thenReturn(Cohort.builder().id(id).entryYear(2024).build());
    when(studentRepository.findByCohortId(id)).thenReturn(List.of());
    when(groupRepository.findByCohortId(id)).thenReturn(List.of(mock(JGroup.class)));
    assertThatThrownBy(() -> subject.delete(id)).isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void delete_without_links_ok() {
    var id = randomUUID();
    var jCohort = mock(JCohort.class);
    when(repository.findById(id)).thenReturn(Optional.of(jCohort));
    when(mapper.toModel(jCohort)).thenReturn(Cohort.builder().id(id).entryYear(2024).build());
    when(studentRepository.findByCohortId(id)).thenReturn(List.of());
    when(groupRepository.findByCohortId(id)).thenReturn(List.of());
    subject.delete(id);
  }
}
