package school.hei.students.service;

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
import school.hei.students.mapper.TeacherMapper;
import school.hei.students.model.Teacher;
import school.hei.students.repository.CourseAssignmentTeachingRepository;
import school.hei.students.repository.TeacherRepository;
import school.hei.students.repository.model.JCourseAssignmentTeaching;
import school.hei.students.repository.model.JTeacher;
import school.hei.students.service.exception.TeacherNotFoundException;

class TeacherServiceTest {
  private TeacherRepository repository;
  private TeacherMapper mapper;
  private CourseAssignmentTeachingRepository teachingRepository;
  private TeacherService subject;

  @BeforeEach
  void setUp() {
    repository = mock(TeacherRepository.class);
    mapper = mock(TeacherMapper.class);
    teachingRepository = mock(CourseAssignmentTeachingRepository.class);
    subject = new TeacherService(repository, mapper, teachingRepository);
  }

  @Test
  void update_with_null_keeps_existing_ok() {
    var id = randomUUID();
    var jTeacher = mock(JTeacher.class);
    var existing =
        Teacher.builder()
            .id(id)
            .userId(randomUUID())
            .lastName("Rabe")
            .firstName("Paul")
            .active(true)
            .build();
    when(repository.findById(id)).thenReturn(Optional.of(jTeacher));
    when(mapper.toModel(jTeacher)).thenReturn(existing);
    when(mapper.toEntity(any())).thenReturn(jTeacher);
    when(repository.save(jTeacher)).thenReturn(jTeacher);
    var result = subject.update(id, null, null);
    assertThat(result).isEqualTo(existing);
  }

  @Test
  void deactivate_not_found_ko() {
    var id = randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> subject.deactivate(id)).isInstanceOf(TeacherNotFoundException.class);
  }

  @Test
  void deactivate_ok() {
    var id = randomUUID();
    var jTeacher = mock(JTeacher.class);
    when(repository.findById(id)).thenReturn(Optional.of(jTeacher));
    subject.deactivate(id);
    verify(jTeacher).setActive(false);
  }

  @Test
  void hasTeaching_true() {
    var id = randomUUID();
    when(teachingRepository.findByTeacherId(id))
        .thenReturn(List.of(mock(JCourseAssignmentTeaching.class)));
    assertThat(subject.hasTeaching(id)).isTrue();
  }

  @Test
  void hasTeaching_false() {
    var id = randomUUID();
    when(teachingRepository.findByTeacherId(id)).thenReturn(List.of());
    assertThat(subject.hasTeaching(id)).isFalse();
  }
}
