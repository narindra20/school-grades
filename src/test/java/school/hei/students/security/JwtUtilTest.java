package school.hei.students.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import school.hei.students.model.Role;

class JwtUtilTest {
  private JwtUtil subject;

  @BeforeEach
  void setUp() {
    subject = new JwtUtil("unit-test-jwt-key-not-a-real-secret-32chars", 60000L);
  }

  @Test
  void generate_and_extract_ok() {
    var email = "admin@hei.school";
    var token = subject.generateToken(email, Role.ADMIN.name());
    assertThat(subject.isValid(token)).isTrue();
    assertThat(subject.extractEmail(token)).isEqualTo(email);
    assertThat(subject.extractRole(token)).isEqualTo(Role.ADMIN.name());
  }

  @Test
  void isValid_malformed_token_ko() {
    assertThat(subject.isValid("not-a-real-token")).isFalse();
  }

  @Test
  void isValid_empty_token_ko() {
    assertThat(subject.isValid("")).isFalse();
  }

  @Test
  void isValid_tampered_token_ko() {
    var token = subject.generateToken("student@hei.school", Role.STUDENT.name());
    var tampered = token.substring(0, token.length() - 2) + "xx";
    assertThat(subject.isValid(tampered)).isFalse();
  }
}
