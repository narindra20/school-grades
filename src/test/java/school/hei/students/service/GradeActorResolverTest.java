package school.hei.students.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.model.Role;
import school.hei.students.repository.StudentRepository;
import school.hei.students.repository.TeacherRepository;
import school.hei.students.repository.UserRepository;
import school.hei.students.repository.model.JStudent;
import school.hei.students.repository.model.JTeacher;
import school.hei.students.repository.model.JUser;

@ExtendWith(MockitoExtension.class)
class GradeActorResolverTest {
  @Mock private UserRepository userRepository;
  @Mock private StudentRepository studentRepository;
  @Mock private TeacherRepository teacherRepository;
  private GradeActorResolver resolver;

  @BeforeEach
  void setUp() {
    resolver = new GradeActorResolver(userRepository, studentRepository, teacherRepository);
  }

  @Test
  void throws_unauthorized_when_authentication_is_null() {
    assertThatThrownBy(() -> resolver.resolve(null)).isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void throws_unauthorized_when_user_unknown() {
    var authentication =
        new UsernamePasswordAuthenticationToken(
            "ghost@hei.school", null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    when(userRepository.findByEmail("ghost@hei.school")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> resolver.resolve(authentication))
        .isInstanceOf(ResponseStatusException.class);
  }

  @Test
  void resolves_student_actor_with_student_id() {
    var userId = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var authentication =
        new UsernamePasswordAuthenticationToken(
            "student@hei.school",
            null,
            java.util.List.of(new SimpleGrantedAuthority("ROLE_STUDENT")));
    when(userRepository.findByEmail("student@hei.school"))
        .thenReturn(Optional.of(JUser.builder().id(userId).role("STUDENT").build()));
    when(studentRepository.findByUserId(userId))
        .thenReturn(Optional.of(JStudent.builder().id(studentId).userId(userId).build()));
    var actor = resolver.resolve(authentication);
    assertThat(actor.role()).isEqualTo(Role.STUDENT);
    assertThat(actor.studentId()).isEqualTo(studentId);
    assertThat(actor.teacherId()).isNull();
  }

  @Test
  void resolves_teacher_actor_with_teacher_id() {
    var userId = UUID.randomUUID();
    var teacherId = UUID.randomUUID();
    var authentication =
        new UsernamePasswordAuthenticationToken(
            "teacher@hei.school",
            null,
            java.util.List.of(new SimpleGrantedAuthority("ROLE_TEACHER")));
    when(userRepository.findByEmail("teacher@hei.school"))
        .thenReturn(Optional.of(JUser.builder().id(userId).role("TEACHER").build()));
    when(teacherRepository.findByUserId(userId))
        .thenReturn(Optional.of(JTeacher.builder().id(teacherId).userId(userId).build()));
    var actor = resolver.resolve(authentication);
    assertThat(actor.role()).isEqualTo(Role.TEACHER);
    assertThat(actor.teacherId()).isEqualTo(teacherId);
    assertThat(actor.studentId()).isNull();
  }

  @Test
  void resolves_admin_actor_without_student_or_teacher_id() {
    var userId = UUID.randomUUID();
    var authentication =
        new UsernamePasswordAuthenticationToken(
            "admin@hei.school", null, java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
    when(userRepository.findByEmail("admin@hei.school"))
        .thenReturn(Optional.of(JUser.builder().id(userId).role("ADMIN").build()));
    var actor = resolver.resolve(authentication);
    assertThat(actor.role()).isEqualTo(Role.ADMIN);
    assertThat(actor.studentId()).isNull();
    assertThat(actor.teacherId()).isNull();
  }
}
