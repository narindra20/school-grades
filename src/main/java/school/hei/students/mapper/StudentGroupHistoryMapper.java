package school.hei.students.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.students.model.StudentGroupHistory;
import school.hei.students.repository.model.JStudentGroupHistory;

@Component
public class StudentGroupHistoryMapper {
  public StudentGroupHistory toModel(JStudentGroupHistory entity) {
    return StudentGroupHistory.builder()
        .id(entity.getId())
        .studentId(entity.getStudentId())
        .groupId(entity.getGroupId())
        .startDate(entity.getStartDate())
        .endDate(entity.getEndDate())
        .build();
  }

  public List<StudentGroupHistory> toModel(List<JStudentGroupHistory> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JStudentGroupHistory toEntity(StudentGroupHistory model) {
    return JStudentGroupHistory.builder()
        .id(model.id())
        .studentId(model.studentId())
        .groupId(model.groupId())
        .startDate(model.startDate())
        .endDate(model.endDate())
        .build();
  }
}
