package school.hei.students.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.students.model.CourseAssignment;
import school.hei.students.repository.model.JCourseAssignment;

@Component
public class CourseAssignmentMapper {
  public CourseAssignment toModel(JCourseAssignment entity) {
    return CourseAssignment.builder()
        .id(entity.getId())
        .courseId(entity.getCourseId())
        .academicYear(entity.getAcademicYear())
        .build();
  }

  public List<CourseAssignment> toModel(List<JCourseAssignment> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JCourseAssignment toEntity(CourseAssignment model) {
    return JCourseAssignment.builder()
        .id(model.id())
        .courseId(model.courseId())
        .academicYear(model.academicYear())
        .build();
  }
}
