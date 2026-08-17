package school.hei.students.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record GradeActor(Role role, UUID studentId, UUID teacherId) {
  public boolean isAdmin() {
    return role == Role.ADMIN;
  }

  public boolean isTeacher() {
    return role == Role.TEACHER;
  }

  public boolean isStudent() {
    return role == Role.STUDENT;
  }
}
