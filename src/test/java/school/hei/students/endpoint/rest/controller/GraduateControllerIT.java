package school.hei.students.endpoint.rest.controller;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

import java.time.Instant;
import java.util.UUID;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import school.hei.students.conf.FacadeIT;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.model.Role;
import school.hei.students.repository.CohortRepository;
import school.hei.students.repository.CourseAssignmentRepository;
import school.hei.students.repository.CourseRepository;
import school.hei.students.repository.ExamRepository;
import school.hei.students.repository.GradeRepository;
import school.hei.students.repository.StudentRepository;
import school.hei.students.repository.model.JCohort;
import school.hei.students.repository.model.JCourse;
import school.hei.students.repository.model.JCourseAssignment;
import school.hei.students.repository.model.JExam;
import school.hei.students.repository.model.JGrade;
import school.hei.students.repository.model.JStudent;
import school.hei.students.service.UserService;

class GraduateControllerIT extends FacadeIT {
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserService userService;
  @Autowired private CohortRepository cohortRepository;
  @Autowired private StudentRepository studentRepository;
  @Autowired private CourseRepository courseRepository;
  @Autowired private CourseAssignmentRepository courseAssignmentRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private GradeRepository gradeRepository;

  private String login(String email, String password) {
    var response =
        restTemplate.postForEntity(
            "/auth/login", new LoginRequest(email, password), LoginResponse.class);
    return response.getBody().token();
  }

  private HttpHeaders headers(String token) {
    var headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  private String adminToken() {
    var email = "graduate-it-admin-" + randomUUID() + "@hei.school";
    userService.create(randomUUID(), email, "test-password-only", Role.ADMIN);
    return login(email, "test-password-only");
  }

  private String userEmailOf(UUID userId) {
    return userService.getById(userId).email();
  }

  private JCohort createCohort() {
    return cohortRepository.save(JCohort.builder().entryYear(2025).build());
  }

  private JStudent createStudent(UUID cohortId) {
    var uniqueSuffix = randomUUID();
    var email = "graduate-it-student-" + uniqueSuffix + "@hei.school";
    var user = userService.create(randomUUID(), email, "test-password-only", Role.STUDENT);
    return studentRepository.save(
        JStudent.builder()
            .userId(user.id())
            .cohortId(cohortId)
            .lastName("Rakoto")
            .firstName("Fitia")
            .studentNumber(uniqueSuffix.toString())
            .workStudy(false)
            .active(true)
            .build());
  }

  private JCourseAssignment createCourseAssignment(int credits) {
    var course =
        courseRepository.save(
            JCourse.builder()
                .code("GRADUATE-IT-" + randomUUID())
                .title("Graduate IT course")
                .credits(credits)
                .level("L1")
                .semester("S1")
                .build());
    return courseAssignmentRepository.save(
        JCourseAssignment.builder().courseId(course.getId()).academicYear("2025-2026").build());
  }

  private JExam createExam(UUID assignmentId) {
    return examRepository.save(
        JExam.builder()
            .assignmentId(assignmentId)
            .label("Only exam")
            .examDate(Instant.now())
            .coefficient(1.0)
            .type("REGULAR")
            .build());
  }

  private void grade(UUID studentId, UUID examId, double value) {
    gradeRepository.save(
        JGrade.builder()
            .studentId(studentId)
            .examId(examId)
            .value(value)
            .gradedDate(java.time.LocalDate.now())
            .build());
  }

  @Test
  void admin_downloads_xlsx_with_only_graduating_students() throws Exception {
    var cohort = createCohort();
    var assignment = createCourseAssignment(60);
    var exam = createExam(assignment.getId());

    var graduate = createStudent(cohort.getId());
    grade(graduate.getId(), exam.getId(), 16.0);

    var nonGraduate = createStudent(cohort.getId());
    grade(nonGraduate.getId(), exam.getId(), 6.0);

    var token = adminToken();
    var response =
        restTemplate.exchange(
            "/cohorts/" + cohort.getId() + "/graduates/export",
            GET,
            new HttpEntity<>(headers(token)),
            byte[].class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getContentDisposition().getFilename())
        .isEqualTo("graduates-" + cohort.getId() + ".xlsx");

    var tempFile = java.io.File.createTempFile("test-graduates-", ".xlsx");
    java.nio.file.Files.write(tempFile.toPath(), response.getBody());
    try (var workbook = WorkbookFactory.create(tempFile)) {
      var sheet = workbook.getSheetAt(0);
      assertThat(sheet.getLastRowNum()).isEqualTo(1);
      var row = sheet.getRow(1);
      assertThat(row.getCell(1).getStringCellValue()).isEqualTo(graduate.getStudentNumber());
      assertThat(row.getCell(4).getNumericCellValue()).isEqualTo(16.0);
    }
    tempFile.delete();
  }

  @Test
  void non_admin_cannot_export_graduates() {
    var cohort = createCohort();
    var student = createStudent(cohort.getId());
    var token = login(userEmailOf(student.getUserId()), "test-password-only");

    var response =
        restTemplate.exchange(
            "/cohorts/" + cohort.getId() + "/graduates/export",
            GET,
            new HttpEntity<>(headers(token)),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void unknown_cohort_returns_not_found() {
    var token = adminToken();
    var response =
        restTemplate.exchange(
            "/cohorts/" + randomUUID() + "/graduates/export",
            GET,
            new HttpEntity<>(headers(token)),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void access_denied_without_token() {
    var cohort = createCohort();
    var response =
        restTemplate.exchange(
            "/cohorts/" + cohort.getId() + "/graduates/export",
            GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
  }
}
