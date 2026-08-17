package school.hei.students.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.students.model.Teacher;
import school.hei.students.repository.model.JTeacher;

@Component
public class TeacherMapper {
  public Teacher toModel(JTeacher entity) {
    return Teacher.builder()
        .id(entity.getId())
        .userId(entity.getUserId())
        .lastName(entity.getLastName())
        .firstName(entity.getFirstName())
        .active(entity.isActive())
        .build();
  }

  public List<Teacher> toModel(List<JTeacher> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JTeacher toEntity(Teacher model) {
    return JTeacher.builder()
        .id(model.id())
        .userId(model.userId())
        .lastName(model.lastName())
        .firstName(model.firstName())
        .active(model.active())
        .build();
  }
}
