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
import org.springframework.security.crypto.password.PasswordEncoder;
import school.hei.students.mapper.UserMapper;
import school.hei.students.model.Role;
import school.hei.students.model.User;
import school.hei.students.repository.UserRepository;
import school.hei.students.repository.model.JUser;

class UserServiceTest {
  private UserRepository repository;
  private UserMapper mapper;
  private PasswordEncoder passwordEncoder;
  private UserService subject;

  @BeforeEach
  void setUp() {
    repository = mock(UserRepository.class);
    mapper = mock(UserMapper.class);
    passwordEncoder = mock(PasswordEncoder.class);
    subject = new UserService(repository, mapper, passwordEncoder);
  }

  @Test
  void create_ok() {
    var id = randomUUID();
    var jUser = mock(JUser.class);
    var user =
        User.builder()
            .id(id)
            .email("a@hei.school")
            .password("hashed")
            .role(Role.ADMIN)
            .active(true)
            .build();
    when(passwordEncoder.encode("test-password-only")).thenReturn("hashed");
    when(mapper.toEntity(any())).thenReturn(jUser);
    when(repository.save(jUser)).thenReturn(jUser);
    when(mapper.toModel(jUser)).thenReturn(user);
    var result = subject.create(id, "a@hei.school", "test-password-only", Role.ADMIN);
    assertThat(result).isEqualTo(user);
    verify(passwordEncoder).encode("test-password-only");
    verify(repository).save(jUser);
  }

  @Test
  void getById_ok() {
    var id = randomUUID();
    var jUser = mock(JUser.class);
    var user = User.builder().id(id).email("a@hei.school").role(Role.STUDENT).active(true).build();
    when(repository.findById(id)).thenReturn(Optional.of(jUser));
    when(mapper.toModel(jUser)).thenReturn(user);
    var result = subject.getById(id);
    assertThat(result).isEqualTo(user);
  }

  @Test
  void getById_not_found_ko() {
    var id = randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> subject.getById(id))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining(id.toString());
  }

  @Test
  void getByEmail_ok() {
    var jUser = mock(JUser.class);
    var user =
        User.builder()
            .id(randomUUID())
            .email("a@hei.school")
            .role(Role.TEACHER)
            .active(true)
            .build();
    when(repository.findByEmail("a@hei.school")).thenReturn(Optional.of(jUser));
    when(mapper.toModel(jUser)).thenReturn(user);
    var result = subject.getByEmail("a@hei.school");
    assertThat(result).isEqualTo(user);
  }

  @Test
  void getByEmail_not_found_ko() {
    when(repository.findByEmail("unknown@hei.school")).thenReturn(Optional.empty());
    assertThatThrownBy(() -> subject.getByEmail("unknown@hei.school"))
        .isInstanceOf(UserNotFoundException.class)
        .hasMessageContaining("unknown@hei.school");
  }

  @Test
  void getAll_ok() {
    var jUsers = List.of(mock(JUser.class));
    var users =
        List.of(
            User.builder()
                .id(randomUUID())
                .email("a@hei.school")
                .role(Role.ADMIN)
                .active(true)
                .build());
    when(repository.findAll()).thenReturn(jUsers);
    when(mapper.toModel(jUsers)).thenReturn(users);
    var result = subject.getAll();
    assertThat(result).isEqualTo(users);
  }

  @Test
  void update_with_all_fields_ok() {
    var id = randomUUID();
    var existingEntity = mock(JUser.class);
    var existing =
        User.builder()
            .id(id)
            .email("old@hei.school")
            .password("oldhash")
            .role(Role.STUDENT)
            .active(true)
            .build();
    var savedEntity = mock(JUser.class);
    var updated =
        User.builder()
            .id(id)
            .email("new@hei.school")
            .password("newhash")
            .role(Role.TEACHER)
            .active(true)
            .build();
    when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
    when(mapper.toModel(existingEntity)).thenReturn(existing);
    when(passwordEncoder.encode("newpass")).thenReturn("newhash");
    when(mapper.toEntity(any())).thenReturn(savedEntity);
    when(repository.save(savedEntity)).thenReturn(savedEntity);
    when(mapper.toModel(savedEntity)).thenReturn(updated);
    var result = subject.update(id, "new@hei.school", "newpass", Role.TEACHER);
    assertThat(result).isEqualTo(updated);
  }

  @Test
  void update_with_null_fields_keeps_existing_ok() {
    var id = randomUUID();
    var existingEntity = mock(JUser.class);
    var existing =
        User.builder()
            .id(id)
            .email("old@hei.school")
            .password("oldhash")
            .role(Role.STUDENT)
            .active(true)
            .build();
    var savedEntity = mock(JUser.class);
    when(repository.findById(id)).thenReturn(Optional.of(existingEntity));
    when(mapper.toModel(existingEntity)).thenReturn(existing);
    when(mapper.toEntity(any())).thenReturn(savedEntity);
    when(repository.save(savedEntity)).thenReturn(savedEntity);
    when(mapper.toModel(savedEntity)).thenReturn(existing);
    var result = subject.update(id, null, null, null);
    assertThat(result.email()).isEqualTo("old@hei.school");
    assertThat(result.password()).isEqualTo("oldhash");
    assertThat(result.role()).isEqualTo(Role.STUDENT);
  }

  @Test
  void update_not_found_ko() {
    var id = randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> subject.update(id, "a@hei.school", "pass", Role.ADMIN))
        .isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void deactivate_ok() {
    var id = randomUUID();
    var jUser = mock(JUser.class);
    when(repository.findById(id)).thenReturn(Optional.of(jUser));
    subject.deactivate(id);
    verify(jUser).setActive(false);
    verify(repository).save(jUser);
  }

  @Test
  void deactivate_not_found_ko() {
    var id = randomUUID();
    when(repository.findById(id)).thenReturn(Optional.empty());
    assertThatThrownBy(() -> subject.deactivate(id)).isInstanceOf(UserNotFoundException.class);
  }

  @Test
  void matchesPassword_true() {
    when(passwordEncoder.matches("raw", "encoded")).thenReturn(true);
    assertThat(subject.matchesPassword("raw", "encoded")).isTrue();
  }

  @Test
  void matchesPassword_false() {
    when(passwordEncoder.matches("wrong", "encoded")).thenReturn(false);
    assertThat(subject.matchesPassword("wrong", "encoded")).isFalse();
  }
}
