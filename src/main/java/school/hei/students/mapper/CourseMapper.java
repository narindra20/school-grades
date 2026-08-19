package school.hei.students.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.students.model.Course;
import school.hei.students.repository.model.JCourse;

@Component
public class CourseMapper {
  public Course toModel(JCourse entity) {
    return Course.builder()
        .id(entity.getId())
        .code(entity.getCode())
        .title(entity.getTitle())
        .credits(entity.getCredits())
        .level(entity.getLevel())
        .semester(entity.getSemester())
        .build();
  }

  public List<Course> toModel(List<JCourse> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JCourse toEntity(Course model) {
    return JCourse.builder()
        .id(model.id())
        .code(model.code())
        .title(model.title())
        .credits(model.credits())
        .level(model.level())
        .semester(model.semester())
        .build();
  }
}
