package school.hei.students.service;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.mapper.GroupMapper;
import school.hei.students.model.Group;
import school.hei.students.repository.CourseAssignmentTeachingRepository;
import school.hei.students.repository.GroupRepository;
import school.hei.students.repository.StudentGroupHistoryRepository;
import school.hei.students.repository.model.JCourseAssignmentTeaching;
import school.hei.students.repository.model.JGroup;
import school.hei.students.repository.model.JStudentGroupHistory;
import school.hei.students.service.exception.GroupNotFoundException;

class GroupServiceTest {
  private GroupRepository repository;
  private GroupMapper mapper;
  private StudentGroupHistoryRepository historyRepository;
  private CourseAssignmentTeachingRepository teachingRepository;
  private GroupService subject;

  @BeforeEach
  void setUp() {
    repository = mock(GroupRepository.class);
    mapper = mock(GroupMapper.class);
    historyRepository = mock(StudentGroupHistoryRepository.class);
    teachingRepository = mock(CourseAssignmentTeachingRepository.class);
    subject = new GroupService(repository, mapper, historyRepository, teachingRepository);
  }

  @Test
  void getAll_no_filter_ok() {
    when(repository.findAll()).thenReturn(List.of());
    when(mapper.toModel(List.<JGroup>of())).thenReturn(List.of());
    var result = subject.getAll(null, null);
    assertThat(result).isEmpty();
  }

  @Test
  void getById_not_found_ko() {
    var id = randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> subject.getById(id)).isInstanceOf(GroupNotFoundException.class);
  }

  @Test
  void delete_with_linked_history_ko() {
    var id = randomUUID();
    var jGroup = mock(JGroup.class);
    when(repository.findById(id)).thenReturn(Optional.of(jGroup));
    when(mapper.toModel(jGroup)).thenReturn(Group.builder().id(id).code("K1").build());
    when(historyRepository.findByGroupId(id)).thenReturn(List.of(mock(JStudentGroupHistory.class)));
    when(teachingRepository.findByGroupId(id)).thenReturn(List.of());
    assertThatThrownBy(() -> subject.delete(id)).isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void delete_with_linked_teaching_ko() {
    var id = randomUUID();
    var jGroup = mock(JGroup.class);
    when(repository.findById(id)).thenReturn(Optional.of(jGroup));
    when(mapper.toModel(jGroup)).thenReturn(Group.builder().id(id).code("K1").build());
    when(historyRepository.findByGroupId(id)).thenReturn(List.of());
    when(teachingRepository.findByGroupId(id))
        .thenReturn(List.of(mock(JCourseAssignmentTeaching.class)));
    assertThatThrownBy(() -> subject.delete(id)).isInstanceOf(ResponseStatusException.class);
  }
}
