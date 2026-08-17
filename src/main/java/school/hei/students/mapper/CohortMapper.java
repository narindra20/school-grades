package school.hei.students.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.students.model.Cohort;
import school.hei.students.repository.model.JCohort;

@Component
public class CohortMapper {
  public Cohort toModel(JCohort entity) {
    return Cohort.builder().id(entity.getId()).entryYear(entity.getEntryYear()).build();
  }

  public List<Cohort> toModel(List<JCohort> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JCohort toEntity(Cohort model) {
    return JCohort.builder().id(model.id()).entryYear(model.entryYear()).build();
  }
}
