package school.hei.students.service.event;

import jakarta.mail.internet.AddressException;
import jakarta.mail.internet.InternetAddress;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import school.hei.students.endpoint.event.model.TranscriptRequested;
import school.hei.students.file.bucket.BucketComponent;
import school.hei.students.mail.Email;
import school.hei.students.mail.Mailer;
import school.hei.students.model.DeliveryStatus;
import school.hei.students.pdf.TranscriptCourseLine;
import school.hei.students.pdf.TranscriptData;
import school.hei.students.pdf.TranscriptPdfGenerator;
import school.hei.students.repository.CourseAssignmentRepository;
import school.hei.students.repository.CourseRepository;
import school.hei.students.repository.ExamRepository;
import school.hei.students.repository.GradeRepository;
import school.hei.students.repository.StudentRepository;
import school.hei.students.repository.TranscriptDeliveryRepository;
import school.hei.students.repository.UserRepository;
import school.hei.students.repository.model.JCourse;
import school.hei.students.repository.model.JCourseAssignment;
import school.hei.students.repository.model.JExam;
import school.hei.students.repository.model.JGrade;
import school.hei.students.repository.model.JTranscriptDelivery;
import school.hei.students.repository.model.JUser;
import school.hei.students.service.exception.StudentNotFoundException;
import school.hei.students.service.exception.TranscriptDeliveryNotFoundException;
import school.hei.students.service.exception.UserNotFoundException;

@Service
@AllArgsConstructor
@Slf4j
public class TranscriptRequestedService implements Consumer<TranscriptRequested> {

  private static final String BUCKET_KEY_TEMPLATE = "transcripts/%s/%s/%s.pdf";

  private final TranscriptDeliveryRepository transcriptDeliveryRepository;
  private final StudentRepository studentRepository;
  private final UserRepository userRepository;
  private final CourseAssignmentRepository courseAssignmentRepository;
  private final ExamRepository examRepository;
  private final GradeRepository gradeRepository;
  private final CourseRepository courseRepository;
  private final TranscriptPdfGenerator pdfGenerator;
  private final BucketComponent bucketComponent;
  private final Mailer mailer;

  @Override
  public void accept(TranscriptRequested event) {
    var delivery =
        transcriptDeliveryRepository
            .findById(event.getTranscriptDeliveryId())
            .orElseThrow(
                () -> new TranscriptDeliveryNotFoundException(event.getTranscriptDeliveryId()));

    if (DeliveryStatus.SENT.name().equals(delivery.getStatus())) {
      log.info("Transcript delivery {} already sent, skipping retry", delivery.getId());
      return;
    }
    try {
      process(event, delivery);
    } catch (Exception e) {
      log.error("Transcript delivery {} failed", delivery.getId(), e);
      markFailed(delivery);
      throw new RuntimeException("Failed to process transcript delivery " + delivery.getId(), e);
    }
  }

  private void process(TranscriptRequested event, JTranscriptDelivery delivery) throws Exception {
    var student =
        studentRepository
            .findById(event.getStudentId())
            .orElseThrow(() -> new StudentNotFoundException(event.getStudentId()));
    var user =
        userRepository
            .findById(student.getUserId())
            .orElseThrow(() -> new UserNotFoundException(student.getUserId()));

    var transcriptData = buildTranscriptData(student, event.getAcademicYear());
    var pdfFile = pdfGenerator.generate(transcriptData);

    var bucketKey =
        BUCKET_KEY_TEMPLATE.formatted(
            event.getStudentId(), event.getAcademicYear(), delivery.getId());
    bucketComponent.upload(pdfFile, bucketKey);

    mailer.accept(toEmail(user, transcriptData, pdfFile));

    markSent(delivery, bucketKey);
  }

  private TranscriptData buildTranscriptData(
      school.hei.students.repository.model.JStudent student, String academicYear) {
    var assignmentsOfYear = courseAssignmentRepository.findByAcademicYear(academicYear);
    Map<java.util.UUID, JExam> examsById = new HashMap<>();
    Map<java.util.UUID, JCourseAssignment> assignmentsById = new HashMap<>();
    for (var assignment : assignmentsOfYear) {
      assignmentsById.put(assignment.getId(), assignment);
      examRepository
          .findByAssignmentId(assignment.getId())
          .forEach(exam -> examsById.put(exam.getId(), exam));
    }

    var studentGrades =
        gradeRepository.findByStudentId(student.getId()).stream()
            .filter(grade -> examsById.containsKey(grade.getExamId()))
            .toList();
    Map<java.util.UUID, double[]> weightedSumByCourse = new HashMap<>();
    for (JGrade grade : studentGrades) {
      var exam = examsById.get(grade.getExamId());
      var assignment = assignmentsById.get(exam.getAssignmentId());
      var acc = weightedSumByCourse.computeIfAbsent(assignment.getCourseId(), c -> new double[2]);
      var coefficient = exam.getCoefficient() == null ? 1.0 : exam.getCoefficient();
      acc[0] += grade.getValue() * coefficient;
      acc[1] += coefficient;
    }

    List<TranscriptCourseLine> lines = new java.util.ArrayList<>();
    double creditWeightedSum = 0;
    double creditSum = 0;
    for (var entry : weightedSumByCourse.entrySet()) {
      JCourse course = courseRepository.findById(entry.getKey()).orElse(null);
      if (course == null) {
        continue;
      }
      var acc = entry.getValue();
      double courseAverage = acc[1] == 0 ? 0 : acc[0] / acc[1];
      lines.add(
          TranscriptCourseLine.builder()
              .courseCode(course.getCode())
              .courseTitle(course.getTitle())
              .credits(course.getCredits())
              .average(courseAverage)
              .build());
      creditWeightedSum += courseAverage * course.getCredits();
      creditSum += course.getCredits();
    }
    lines.sort(Comparator.comparing(TranscriptCourseLine::courseCode));

    Double overallAverage = creditSum == 0 ? null : creditWeightedSum / creditSum;

    return TranscriptData.builder()
        .studentFirstName(student.getFirstName())
        .studentLastName(student.getLastName())
        .studentNumber(student.getStudentNumber())
        .academicYear(academicYear)
        .lines(lines)
        .overallAverage(overallAverage)
        .build();
  }

  private Email toEmail(JUser user, TranscriptData data, java.io.File pdfFile)
      throws AddressException {
    return new Email(
        new InternetAddress(user.getEmail()),
        List.of(),
        List.of(),
        "Votre relevé de notes — " + data.academicYear(),
        "<p>Bonjour "
            + data.studentFirstName()
            + ",</p><p>Veuillez trouver ci-joint votre relevé de notes pour l'année académique "
            + data.academicYear()
            + ".</p><p>Cordialement,<br/>HEI</p>",
        List.of(pdfFile));
  }

  private void markSent(JTranscriptDelivery delivery, String bucketKey) {
    delivery.setStatus(DeliveryStatus.SENT.name());
    delivery.setS3Url(bucketKey);
    delivery.setSentAt(Instant.now());
    transcriptDeliveryRepository.save(delivery);
  }

  private void markFailed(JTranscriptDelivery delivery) {
    delivery.setStatus(DeliveryStatus.FAILED.name());
    transcriptDeliveryRepository.save(delivery);
  }
}
