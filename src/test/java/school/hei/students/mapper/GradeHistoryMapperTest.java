package school.hei.students.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import school.hei.students.model.GradeHistory;
import school.hei.students.repository.model.JGradeHistory;

class GradeHistoryMapperTest {
  private final GradeHistoryMapper mapper = new GradeHistoryMapper();

  @Test
  void to_model_maps_all_fields() {
    var id = UUID.randomUUID();
    var gradeId = UUID.randomUUID();
    var modifiedAt = Instant.parse("2026-01-20T10:15:30Z");
    var entity =
        JGradeHistory.builder()
            .id(id)
            .gradeId(gradeId)
            .oldValue(8.0)
            .newValue(11.0)
            .reason("Student dispute")
            .modifiedAt(modifiedAt)
            .build();
    var model = mapper.toModel(entity);
    assertThat(model.id()).isEqualTo(id);
    assertThat(model.gradeId()).isEqualTo(gradeId);
    assertThat(model.oldValue()).isEqualTo(8.0);
    assertThat(model.newValue()).isEqualTo(11.0);
    assertThat(model.reason()).isEqualTo("Student dispute");
    assertThat(model.modifiedAt()).isEqualTo(modifiedAt);
  }

  @Test
  void to_entity_maps_all_fields() {
    var id = UUID.randomUUID();
    var gradeId = UUID.randomUUID();
    var modifiedAt = Instant.parse("2026-02-01T08:00:00Z");
    var model =
        GradeHistory.builder()
            .id(id)
            .gradeId(gradeId)
            .oldValue(5.0)
            .newValue(13.0)
            .reason("Grading error corrected")
            .modifiedAt(modifiedAt)
            .build();
    var entity = mapper.toEntity(model);
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getGradeId()).isEqualTo(gradeId);
    assertThat(entity.getOldValue()).isEqualTo(5.0);
    assertThat(entity.getNewValue()).isEqualTo(13.0);
    assertThat(entity.getReason()).isEqualTo("Grading error corrected");
    assertThat(entity.getModifiedAt()).isEqualTo(modifiedAt);
  }

  @Test
  void to_model_list_maps_every_entity() {
    var entities =
        List.of(
            JGradeHistory.builder().id(UUID.randomUUID()).reason("a").build(),
            JGradeHistory.builder().id(UUID.randomUUID()).reason("b").build());
    var models = mapper.toModel(entities);
    assertThat(models).hasSize(2);
    assertThat(models).extracting(GradeHistory::reason).containsExactly("a", "b");
  }
}
