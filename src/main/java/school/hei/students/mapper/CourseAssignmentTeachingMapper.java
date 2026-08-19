package school.hei.students.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.students.model.CourseAssignmentTeaching;
import school.hei.students.repository.model.JCourseAssignmentTeaching;

@Component
public class CourseAssignmentTeachingMapper {
  public CourseAssignmentTeaching toModel(JCourseAssignmentTeaching entity) {
    return CourseAssignmentTeaching.builder()
        .id(entity.getId())
        .assignmentId(entity.getAssignmentId())
        .teacherId(entity.getTeacherId())
        .groupId(entity.getGroupId())
        .build();
  }

  public List<CourseAssignmentTeaching> toModel(List<JCourseAssignmentTeaching> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JCourseAssignmentTeaching toEntity(CourseAssignmentTeaching model) {
    return JCourseAssignmentTeaching.builder()
        .id(model.id())
        .assignmentId(model.assignmentId())
        .teacherId(model.teacherId())
        .groupId(model.groupId())
        .build();
  }
}
