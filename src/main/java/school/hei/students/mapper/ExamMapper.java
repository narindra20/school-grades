package school.hei.students.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.students.model.Exam;
import school.hei.students.model.ExamType;
import school.hei.students.repository.model.JExam;

@Component
public class ExamMapper {
  public Exam toModel(JExam entity) {
    return Exam.builder()
        .id(entity.getId())
        .assignmentId(entity.getAssignmentId())
        .label(entity.getLabel())
        .examDate(entity.getExamDate())
        .coefficient(entity.getCoefficient())
        .type(ExamType.valueOf(entity.getType()))
        .build();
  }

  public List<Exam> toModel(List<JExam> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JExam toEntity(Exam model) {
    return JExam.builder()
        .id(model.id())
        .assignmentId(model.assignmentId())
        .label(model.label())
        .examDate(model.examDate())
        .coefficient(model.coefficient())
        .type(model.type().name())
        .build();
  }
}
