package school.hei.students.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import school.hei.students.model.Exam;
import school.hei.students.model.ExamType;
import school.hei.students.repository.model.JExam;

class ExamMapperTest {
  private final ExamMapper mapper = new ExamMapper();

  @Test
  void to_model_maps_all_fields_and_parses_type() {
    var id = UUID.randomUUID();
    var assignmentId = UUID.randomUUID();
    var examDate = Instant.parse("2026-05-15T09:00:00Z");
    var entity =
        JExam.builder()
            .id(id)
            .assignmentId(assignmentId)
            .label("OOP")
            .examDate(examDate)
            .coefficient(0.6)
            .type(ExamType.REGULAR.name())
            .build();
    var model = mapper.toModel(entity);
    assertThat(model.id()).isEqualTo(id);
    assertThat(model.assignmentId()).isEqualTo(assignmentId);
    assertThat(model.label()).isEqualTo("OOP");
    assertThat(model.examDate()).isEqualTo(examDate);
    assertThat(model.coefficient()).isEqualTo(0.6);
    assertThat(model.type()).isEqualTo(ExamType.REGULAR);
  }

  @Test
  void to_model_parses_resit_type() {
    var entity =
        JExam.builder()
            .id(UUID.randomUUID())
            .assignmentId(UUID.randomUUID())
            .label("Resit")
            .examDate(Instant.now())
            .coefficient(1.0)
            .type(ExamType.RESIT.name())
            .build();
    assertThat(mapper.toModel(entity).type()).isEqualTo(ExamType.RESIT);
  }

  @Test
  void to_entity_maps_all_fields_and_serializes_type() {
    var id = UUID.randomUUID();
    var assignmentId = UUID.randomUUID();
    var examDate = Instant.parse("2026-06-01T14:00:00Z");
    var model =
        Exam.builder()
            .id(id)
            .assignmentId(assignmentId)
            .label("API")
            .examDate(examDate)
            .coefficient(0.4)
            .type(ExamType.RESIT)
            .build();
    var entity = mapper.toEntity(model);
    assertThat(entity.getId()).isEqualTo(id);
    assertThat(entity.getAssignmentId()).isEqualTo(assignmentId);
    assertThat(entity.getLabel()).isEqualTo("API");
    assertThat(entity.getExamDate()).isEqualTo(examDate);
    assertThat(entity.getCoefficient()).isEqualTo(0.4);
    assertThat(entity.getType()).isEqualTo("RESIT");
  }

  @Test
  void to_model_list_maps_every_entity() {
    var entities =
        List.of(
            JExam.builder().id(UUID.randomUUID()).type(ExamType.REGULAR.name()).build(),
            JExam.builder().id(UUID.randomUUID()).type(ExamType.RESIT.name()).build());
    var models = mapper.toModel(entities);
    assertThat(models).hasSize(2);
    assertThat(models).extracting(Exam::type).containsExactly(ExamType.REGULAR, ExamType.RESIT);
  }
}
