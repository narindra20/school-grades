package school.hei.students.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.students.model.GradeHistory;
import school.hei.students.repository.model.JGradeHistory;

@Component
public class GradeHistoryMapper {
  public GradeHistory toModel(JGradeHistory entity) {
    return GradeHistory.builder()
        .id(entity.getId())
        .gradeId(entity.getGradeId())
        .oldValue(entity.getOldValue())
        .newValue(entity.getNewValue())
        .reason(entity.getReason())
        .modifiedAt(entity.getModifiedAt())
        .build();
  }

  public List<GradeHistory> toModel(List<JGradeHistory> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JGradeHistory toEntity(GradeHistory model) {
    return JGradeHistory.builder()
        .id(model.id())
        .gradeId(model.gradeId())
        .oldValue(model.oldValue())
        .newValue(model.newValue())
        .reason(model.reason())
        .modifiedAt(model.modifiedAt())
        .build();
  }
}
