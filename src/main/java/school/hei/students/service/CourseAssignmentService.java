package school.hei.students.service;

import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpStatus.CONFLICT;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.mapper.CourseAssignmentMapper;
import school.hei.students.mapper.CourseAssignmentTeachingMapper;
import school.hei.students.model.CourseAssignment;
import school.hei.students.model.CourseAssignmentTeaching;
import school.hei.students.repository.CourseAssignmentRepository;
import school.hei.students.repository.CourseAssignmentTeachingRepository;
import school.hei.students.repository.ExamRepository;
import school.hei.students.repository.GradeRepository;
import school.hei.students.repository.StudentGroupHistoryRepository;
import school.hei.students.service.exception.CourseAssignmentNotFoundException;
import school.hei.students.service.exception.CourseAssignmentTeachingNotFoundException;

@Service
@AllArgsConstructor
public class CourseAssignmentService {
  private final CourseAssignmentRepository repository;
  private final CourseAssignmentMapper mapper;
  private final CourseAssignmentTeachingRepository teachingRepository;
  private final CourseAssignmentTeachingMapper teachingMapper;
  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;
  private final StudentGroupHistoryRepository studentGroupHistoryRepository;

  public List<CourseAssignment> getAll(String academicYear) {
    if (academicYear != null) {
      return mapper.toModel(repository.findByAcademicYear(academicYear));
    }
    return mapper.toModel(repository.findAll());
  }

  public CourseAssignment getById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new CourseAssignmentNotFoundException(id)));
  }

  public CourseAssignment create(UUID courseId, String academicYear) {
    var toSave =
        CourseAssignment.builder()
            .id(randomUUID())
            .courseId(courseId)
            .academicYear(academicYear)
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(toSave)));
  }

  public CourseAssignment update(UUID id, String academicYear) {
    var existing = getById(id);
    var updated =
        CourseAssignment.builder()
            .id(existing.id())
            .courseId(existing.courseId())
            .academicYear(academicYear != null ? academicYear : existing.academicYear())
            .build();
    return mapper.toModel(repository.save(mapper.toEntity(updated)));
  }

  public void delete(UUID id) {
    getById(id);
    if (!examRepository.findByAssignmentId(id).isEmpty()) {
      throw new ResponseStatusException(CONFLICT, "Exams are linked to this course assignment");
    }
    if (!teachingRepository.findByAssignmentId(id).isEmpty()) {
      throw new ResponseStatusException(
          CONFLICT, "Teaching assignments are linked to this course assignment");
    }
    repository.deleteById(id);
  }

  public List<CourseAssignmentTeaching> getTeaching(UUID assignmentId) {
    getById(assignmentId);
    return teachingMapper.toModel(teachingRepository.findByAssignmentId(assignmentId));
  }

  public CourseAssignmentTeaching assignTeaching(UUID assignmentId, UUID teacherId, UUID groupId) {
    getById(assignmentId);
    var toSave =
        CourseAssignmentTeaching.builder()
            .id(randomUUID())
            .assignmentId(assignmentId)
            .teacherId(teacherId)
            .groupId(groupId)
            .build();
    return teachingMapper.toModel(teachingRepository.save(teachingMapper.toEntity(toSave)));
  }

  public void removeTeaching(UUID teachingId) {
    var teaching =
        teachingRepository
            .findById(teachingId)
            .orElseThrow(() -> new CourseAssignmentTeachingNotFoundException(teachingId));
    var studentIdsInGroup =
        studentGroupHistoryRepository.findByGroupId(teaching.getGroupId()).stream()
            .map(sgh -> sgh.getStudentId())
            .collect(Collectors.toSet());
    var examIds =
        examRepository.findByAssignmentId(teaching.getAssignmentId()).stream()
            .map(exam -> exam.getId())
            .collect(Collectors.toSet());
    var hasGrades = hasGradesForGroup(examIds, studentIdsInGroup);
    if (hasGrades) {
      throw new ResponseStatusException(
          CONFLICT, "Grades already exist for this teacher/group pair");
    }
    teachingRepository.deleteById(teachingId);
  }

  private boolean hasGradesForGroup(Set<UUID> examIds, Set<UUID> studentIdsInGroup) {
    return examIds.stream()
        .flatMap(examId -> gradeRepository.findByExamId(examId).stream())
        .anyMatch(grade -> studentIdsInGroup.contains(grade.getStudentId()));
  }
}
