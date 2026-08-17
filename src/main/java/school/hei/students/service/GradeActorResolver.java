package school.hei.students.service;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.model.GradeActor;
import school.hei.students.model.Role;
import school.hei.students.repository.StudentRepository;
import school.hei.students.repository.TeacherRepository;
import school.hei.students.repository.UserRepository;

@Component
@AllArgsConstructor
public class GradeActorResolver {
  private final UserRepository userRepository;
  private final StudentRepository studentRepository;
  private final TeacherRepository teacherRepository;

  public GradeActor resolve(Authentication authentication) {
    if (authentication == null || !authentication.isAuthenticated()) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "No authenticated user");
    }
    var user =
        userRepository
            .findByEmail(authentication.getName())
            .orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unknown user"));
    var role = Role.valueOf(user.getRole());
    var studentId =
        role == Role.STUDENT
            ? studentRepository.findByUserId(user.getId()).map(s -> s.getId()).orElse(null)
            : null;
    var teacherId =
        role == Role.TEACHER
            ? teacherRepository.findByUserId(user.getId()).map(t -> t.getId()).orElse(null)
            : null;
    return GradeActor.builder().role(role).studentId(studentId).teacherId(teacherId).build();
  }
}
