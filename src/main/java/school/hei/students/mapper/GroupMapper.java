package school.hei.students.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.students.model.Group;
import school.hei.students.repository.model.JGroup;

@Component
public class GroupMapper {
  public Group toModel(JGroup entity) {
    return Group.builder()
        .id(entity.getId())
        .code(entity.getCode())
        .trackId(entity.getTrackId())
        .cohortId(entity.getCohortId())
        .build();
  }

  public List<Group> toModel(List<JGroup> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JGroup toEntity(Group model) {
    return JGroup.builder()
        .id(model.id())
        .code(model.code())
        .trackId(model.trackId())
        .cohortId(model.cohortId())
        .build();
  }
}
