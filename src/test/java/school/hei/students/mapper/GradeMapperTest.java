package school.hei.students.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import school.hei.students.model.Grade;
import school.hei.students.repository.model.JGrade;

class GradeMapperTest {
  private final GradeMapper mapper = new GradeMapper();

  @Test
  void to_model_maps_all_fields() {
    var id = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var entity =
        JGrade.builder()
            .id(id)
            .studentId(studentId)
            .examId(examId)
            .value(15.5)
            .gradedDate(LocalDate.of(2026, 1, 20))
            .build();
    var model = mapper.toModel(entity);
    assertThat(model.id()).isEqualTo(id);
    assertThat(model.studentId()).isEqualTo(studentId);
    assertThat(model.examId()).isEqualTo(examId);
    assertThat(model.value()).isEqualTo(15.5);
    assertThat(model.gradedDate()).isEqualTo(LocalDate.of(2026, 1, 20));
  }

  @Test
  void to_entity_maps_all_fields() {
    var id = UUID.randomUUID();
    var studentId = UUID.randomUUID();
    var examId = UUID.randomUUID();
    var model =
        Grade.builder()
            .id(id)
            .studentId(studentId)
            .examId(examId)
            .value(9.0)
            .gradedDate(LocalDate.of(2026, 2, 1))
            .build();
    var entity = mapper.toEntity(model);
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getStudentId()).isEqualTo(studentId);
    assertThat(entity.getExamId()).isEqualTo(examId);
    assertThat(entity.getValue()).isEqualTo(9.0);
    assertThat(entity.getGradedDate()).isEqualTo(LocalDate.of(2026, 2, 1));
  }

  @Test
  void to_model_list_maps_every_entity() {
    var entities =
        List.of(
            JGrade.builder().id(UUID.randomUUID()).value(10.0).build(),
            JGrade.builder().id(UUID.randomUUID()).value(12.0).build());
    var models = mapper.toModel(entities);
    assertThat(models).hasSize(2);
    assertThat(models).extracting(Grade::value).containsExactly(10.0, 12.0);
  }
}
