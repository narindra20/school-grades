package school.hei.students.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.students.model.Grade;
import school.hei.students.repository.model.JGrade;

@Component
public class GradeMapper {
  public Grade toModel(JGrade entity) {
    return Grade.builder()
        .id(entity.getId())
        .studentId(entity.getStudentId())
        .examId(entity.getExamId())
        .value(entity.getValue())
        .gradedDate(entity.getGradedDate())
        .build();
  }

  public List<Grade> toModel(List<JGrade> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JGrade toEntity(Grade model) {
    return JGrade.builder()
        .id(model.id())
        .studentId(model.studentId())
        .examId(model.examId())
        .value(model.value())
        .gradedDate(model.gradedDate())
        .build();
  }
}
