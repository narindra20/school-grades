package school.hei.students.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.students.model.Student;
import school.hei.students.repository.model.JStudent;

@Component
public class StudentMapper {
  public Student toModel(JStudent entity) {
    return Student.builder()
        .id(entity.getId())
        .userId(entity.getUserId())
        .cohortId(entity.getCohortId())
        .lastName(entity.getLastName())
        .firstName(entity.getFirstName())
        .studentNumber(entity.getStudentNumber())
        .workStudy(entity.isWorkStudy())
        .active(entity.isActive())
        .build();
  }

  public List<Student> toModel(List<JStudent> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JStudent toEntity(Student model) {
    return JStudent.builder()
        .id(model.id())
        .userId(model.userId())
        .cohortId(model.cohortId())
        .lastName(model.lastName())
        .firstName(model.firstName())
        .studentNumber(model.studentNumber())
        .workStudy(model.workStudy())
        .active(model.active())
        .build();
  }
}
