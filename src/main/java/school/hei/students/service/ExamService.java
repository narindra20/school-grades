package school.hei.students.service;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.endpoint.rest.controller.dto.ExamCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.ExamUpdateRequest;
import school.hei.students.mapper.ExamMapper;
import school.hei.students.model.Exam;
import school.hei.students.model.ExamType;
import school.hei.students.model.GradeActor;
import school.hei.students.repository.ExamRepository;
import school.hei.students.repository.GradeRepository;
import school.hei.students.repository.model.JExam;

@Service
@AllArgsConstructor
public class ExamService {
  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;
  private final ExamMapper examMapper;

  public List<Exam> listExamsForAssignment(UUID assignmentId, GradeActor actor) {
    assertAdmin(actor);
    return examMapper.toModel(examRepository.findByAssignmentId(assignmentId));
  }

  public Exam getExam(UUID examId, GradeActor actor) {
    assertAdmin(actor);
    return examMapper.toModel(findExamEntityOrThrow(examId));
  }

  @Transactional
  public Exam createExam(UUID assignmentId, ExamCreateRequest request, GradeActor actor) {
    assertAdmin(actor);
    assertCoefficientValid(request.coefficient());
    var toSave =
        Exam.builder()
            .assignmentId(assignmentId)
            .label(request.label())
            .examDate(request.examDate())
            .coefficient(request.coefficient())
            .type(request.type() == null ? ExamType.REGULAR : request.type())
            .build();
    return examMapper.toModel(examRepository.save(examMapper.toEntity(toSave)));
  }

  @Transactional
  public Exam updateExam(UUID examId, ExamUpdateRequest request, GradeActor actor) {
    assertAdmin(actor);
    var entity = findExamEntityOrThrow(examId);
    if (request.label() != null) {
      entity.setLabel(request.label());
    }
    if (request.examDate() != null) {
      entity.setExamDate(request.examDate());
    }
    if (request.coefficient() != null) {
      assertCoefficientValid(request.coefficient());
      entity.setCoefficient(request.coefficient());
    }
    return examMapper.toModel(examRepository.save(entity));
  }

  @Transactional
  public void deleteExam(UUID examId, GradeActor actor) {
    assertAdmin(actor);
    var entity = findExamEntityOrThrow(examId);
    if (!gradeRepository.findByExamId(examId).isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.CONFLICT, "Cannot delete exam: grades are linked to it");
    }
    examRepository.delete(entity);
  }

  private void assertAdmin(GradeActor actor) {
    if (!actor.isAdmin()) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only ADMIN can manage exams");
    }
  }

  private void assertCoefficientValid(Double coefficient) {
    if (coefficient == null || coefficient <= 0) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "coefficient must be strictly positive");
    }
  }

  private JExam findExamEntityOrThrow(UUID examId) {
    return examRepository
        .findById(examId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));
  }
}
