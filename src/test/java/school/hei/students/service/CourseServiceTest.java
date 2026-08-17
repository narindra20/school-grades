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
import school.hei.students.mapper.CourseMapper;
import school.hei.students.model.Course;
import school.hei.students.repository.CourseAssignmentRepository;
import school.hei.students.repository.CourseRepository;
import school.hei.students.repository.model.JCourse;
import school.hei.students.repository.model.JCourseAssignment;
import school.hei.students.service.exception.CourseNotFoundException;

class CourseServiceTest {
  private CourseRepository repository;
  private CourseMapper mapper;
  private CourseAssignmentRepository assignmentRepository;
  private CourseService subject;

  @BeforeEach
  void setUp() {
    repository = mock(CourseRepository.class);
    mapper = mock(CourseMapper.class);
    assignmentRepository = mock(CourseAssignmentRepository.class);
    subject = new CourseService(repository, mapper, assignmentRepository);
  }

  @Test
  void getById_not_found_ko() {
    var id = randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> subject.getById(id)).isInstanceOf(CourseNotFoundException.class);
  }

  @Test
  void update_with_null_keeps_existing_ok() {
    var id = randomUUID();
    var jCourse = mock(JCourse.class);
    var existing =
        Course.builder()
            .id(id)
            .code("prog2")
            .title("Prog2")
            .credits(5)
            .level("L2")
            .semester("S1")
            .build();
    when(repository.findById(id)).thenReturn(Optional.of(jCourse));
    when(mapper.toModel(jCourse)).thenReturn(existing);
    when(mapper.toEntity(any())).thenReturn(jCourse);
    when(repository.save(jCourse)).thenReturn(jCourse);
    var result = subject.update(id, null, null, null, null, null);
    assertThat(result).isEqualTo(existing);
  }

  @Test
  void delete_with_linked_assignment_ko() {
    var id = randomUUID();
    var jCourse = mock(JCourse.class);
    when(repository.findById(id)).thenReturn(Optional.of(jCourse));
    when(mapper.toModel(jCourse))
        .thenReturn(
            Course.builder()
                .id(id)
                .code("prog2")
                .title("Prog2")
                .credits(5)
                .level("L2")
                .semester("S1")
                .build());
    when(assignmentRepository.findByCourseId(id))
        .thenReturn(List.of(mock(JCourseAssignment.class)));
    assertThatThrownBy(() -> subject.delete(id)).isInstanceOf(ResponseStatusException.class);
  }
}
