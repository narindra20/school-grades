package school.hei.students.service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.endpoint.rest.controller.dto.GradeCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.GradeUpdateRequest;
import school.hei.students.mapper.GradeHistoryMapper;
import school.hei.students.mapper.GradeMapper;
import school.hei.students.model.ExamType;
import school.hei.students.model.Grade;
import school.hei.students.model.GradeActor;
import school.hei.students.model.GradeHistory;
import school.hei.students.repository.ExamRepository;
import school.hei.students.repository.GradeHistoryRepository;
import school.hei.students.repository.GradeRepository;
import school.hei.students.repository.model.JGrade;

@Service
@AllArgsConstructor
public class GradeService {
  private static final double MIN_GRADE = 0.0;
  private static final double MAX_GRADE = 20.0;
  private static final double PASSING_THRESHOLD = 10.0;
  private final GradeRepository gradeRepository;
  private final GradeHistoryRepository gradeHistoryRepository;
  private final ExamRepository examRepository;
  private final GradeMapper gradeMapper;
  private final GradeHistoryMapper gradeHistoryMapper;
  private final GradeAuthorizationService authorizationService;

  public List<Grade> listGradesForStudent(UUID studentId, GradeActor actor) {
    authorizationService.assertCanReadStudentGrades(actor, studentId);
    return gradeMapper.toModel(gradeRepository.findByStudentId(studentId));
  }

  public Grade getGrade(UUID gradeId, GradeActor actor) {
    var entity = findGradeEntityOrThrow(gradeId);
    authorizationService.assertCanReadStudentGrades(actor, entity.getStudentId());
    return gradeMapper.toModel(entity);
  }

  public List<GradeHistory> getGradeHistory(UUID gradeId, GradeActor actor) {
    var entity = findGradeEntityOrThrow(gradeId);
    authorizationService.assertCanReadStudentGrades(actor, entity.getStudentId());
    return gradeHistoryMapper.toModel(
        gradeHistoryRepository.findByGradeIdOrderByModifiedAtDesc(gradeId));
  }

  @Transactional
  public Grade createGrade(UUID pathStudentId, GradeCreateRequest request, GradeActor actor) {
    authorizationService.assertCanWriteGradeForExam(actor, request.examId(), request.studentId());
    if (!pathStudentId.equals(request.studentId())) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "studentId in path and body must match");
    }
    assertValueInRange(request.value());
    assertResitAllowed(request.examId(), request.studentId());
    var toSave =
        Grade.builder()
            .studentId(request.studentId())
            .examId(request.examId())
            .value(request.value())
            .gradedDate(LocalDate.now())
            .build();
    return gradeMapper.toModel(gradeRepository.save(gradeMapper.toEntity(toSave)));
  }

  @Transactional
  public Grade updateGrade(UUID gradeId, GradeUpdateRequest request, GradeActor actor) {
    if (request.reason() == null || request.reason().isBlank()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "reason is mandatory when updating a grade");
    }
    assertValueInRange(request.value());
    var entity = findGradeEntityOrThrow(gradeId);
    authorizationService.assertCanWriteGradeForExam(
        actor, entity.getExamId(), entity.getStudentId());
    var oldValue = entity.getValue();
    entity.setValue(request.value());
    var updated = gradeRepository.save(entity);
    var history =
        GradeHistory.builder()
            .gradeId(gradeId)
            .oldValue(oldValue)
            .newValue(request.value())
            .reason(request.reason())
            .modifiedAt(Instant.now())
            .build();
    gradeHistoryRepository.save(gradeHistoryMapper.toEntity(history));
    return gradeMapper.toModel(updated);
  }

  private void assertValueInRange(Double value) {
    if (value == null || value < MIN_GRADE || value > MAX_GRADE) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST, "value must be between " + MIN_GRADE + " and " + MAX_GRADE);
    }
  }

  private void assertResitAllowed(UUID examId, UUID studentId) {
    var exam =
        examRepository
            .findById(examId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Exam not found"));
    if (exam.getType() == null || !ExamType.RESIT.name().equals(exam.getType())) {
      return;
    }
    var regularExams =
        examRepository.findByAssignmentId(exam.getAssignmentId()).stream()
            .filter(e -> ExamType.REGULAR.name().equals(e.getType()))
            .collect(Collectors.toList());
    if (regularExams.isEmpty()) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Cannot record a resit grade before regular grades exist for this course");
    }
    var studentGrades = gradeRepository.findByStudentId(studentId);
    var weightedSum = 0.0;
    var coefficientSum = 0.0;
    for (var regular : regularExams) {
      var gradeForExam =
          studentGrades.stream().filter(g -> g.getExamId().equals(regular.getId())).findFirst();
      if (gradeForExam.isEmpty()) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Cannot record a resit grade before all regular grades exist for this course");
      }
      var coefficient = regular.getCoefficient().doubleValue();
      weightedSum += gradeForExam.get().getValue() * coefficient;
      coefficientSum += coefficient;
    }
    var combinedAverage = weightedSum / coefficientSum;
    if (combinedAverage >= PASSING_THRESHOLD) {
      throw new ResponseStatusException(
          HttpStatus.BAD_REQUEST,
          "Cannot record a resit grade: the combined regular average already passed the threshold"
              + " of "
              + PASSING_THRESHOLD);
    }
  }

  private JGrade findGradeEntityOrThrow(UUID gradeId) {
    return gradeRepository
        .findById(gradeId)
        .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Grade not found"));
  }
}
