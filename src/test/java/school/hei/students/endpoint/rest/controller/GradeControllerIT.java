package school.hei.students.endpoint.rest.controller;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.PUT;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import school.hei.students.conf.FacadeIT;
import school.hei.students.endpoint.rest.controller.dto.GradeCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.GradeUpdateRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.model.ExamType;
import school.hei.students.model.Grade;
import school.hei.students.model.GradeHistory;
import school.hei.students.model.Role;
import school.hei.students.repository.CourseAssignmentTeachingRepository;
import school.hei.students.repository.ExamRepository;
import school.hei.students.repository.StudentRepository;
import school.hei.students.repository.TeacherRepository;
import school.hei.students.repository.model.JCourseAssignmentTeaching;
import school.hei.students.repository.model.JExam;
import school.hei.students.repository.model.JStudent;
import school.hei.students.repository.model.JTeacher;
import school.hei.students.service.UserService;

class GradeControllerIT extends FacadeIT {
  private static final AtomicLong STUDENT_NUMBER_SEQ = new AtomicLong();

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserService userService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private StudentRepository studentRepository;
  @Autowired private TeacherRepository teacherRepository;
  @Autowired private ExamRepository examRepository;
  @Autowired private CourseAssignmentTeachingRepository courseAssignmentTeachingRepository;

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

  private UUID insertCourseAssignment() {
    var courseId = randomUUID();
    jdbcTemplate.update(
        "INSERT INTO course (id, code, title, credits, level, semester) VALUES (?, ?, ?, ?, ?, ?)",
        courseId,
        "prog2-" + courseId,
        "Programming 2",
        6,
        "L1",
        "S2");
    var assignmentId = randomUUID();
    jdbcTemplate.update(
        "INSERT INTO course_assignment (id, course_id, academic_year) VALUES (?, ?, ?)",
        assignmentId,
        courseId,
        "2025-2026");
    return assignmentId;
  }

  private UUID insertCohort() {
    var cohortId = randomUUID();
    jdbcTemplate.update("INSERT INTO cohort (id, entry_year) VALUES (?, ?)", cohortId, 2025);
    return cohortId;
  }

  private UUID insertGroup(UUID cohortId) {
    var groupId = randomUUID();
    jdbcTemplate.update(
        "INSERT INTO \"group\" (id, code, cohort_id) VALUES (?, ?, ?)", groupId, "K1", cohortId);
    return groupId;
  }

  private JStudent createStudent(UUID cohortId) {
    var email = "student-" + randomUUID() + "@hei.school";
    var user = userService.create(randomUUID(), email, "test-password-only", Role.STUDENT);
    return studentRepository.save(
        JStudent.builder()
            .userId(user.id())
            .cohortId(cohortId)
            .lastName("Rakoto")
            .firstName("Fitia")
            .studentNumber("25" + STUDENT_NUMBER_SEQ.incrementAndGet())
            .workStudy(false)
            .active(true)
            .build());
  }

  private JTeacher createTeacher() {
    var email = "teacher-" + randomUUID() + "@hei.school";
    var user = userService.create(randomUUID(), email, "test-password-only", Role.TEACHER);
    return teacherRepository.save(
        JTeacher.builder()
            .userId(user.id())
            .lastName("Rabe")
            .firstName("Hery")
            .active(true)
            .build());
  }

  private JExam createExam(UUID assignmentId, ExamType type) {
    return examRepository.save(
        JExam.builder()
            .assignmentId(assignmentId)
            .label(type.name())
            .examDate(Instant.now())
            .coefficient(0.6)
            .type(type.name())
            .build());
  }

  private String adminToken() {
    var email = "admin-" + randomUUID() + "@hei.school";
    userService.create(randomUUID(), email, "test-password-only", Role.ADMIN);
    return login(email, "test-password-only");
  }

  @Test
  void admin_creates_and_reads_grade() {
    var assignmentId = insertCourseAssignment();
    var exam = createExam(assignmentId, ExamType.REGULAR);
    var cohortId = insertCohort();
    var student = createStudent(cohortId);
    var token = adminToken();

    var createRequest =
        GradeCreateRequest.builder()
            .studentId(student.getId())
            .examId(exam.getId())
            .value(15.5)
            .build();
    var createResponse =
        restTemplate.exchange(
            "/students/" + student.getId() + "/grades",
            POST,
            new HttpEntity<>(createRequest, headers(token)),
            Grade.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(createResponse.getBody().value()).isEqualTo(15.5);

    var getResponse =
        restTemplate.exchange(
            "/grades/" + createResponse.getBody().id(),
            GET,
            new HttpEntity<>(headers(token)),
            Grade.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(getResponse.getBody().studentId()).isEqualTo(student.getId());
  }

  @Test
  void create_grade_rejects_out_of_range_value() {
    var assignmentId = insertCourseAssignment();
    var exam = createExam(assignmentId, ExamType.REGULAR);
    var cohortId = insertCohort();
    var student = createStudent(cohortId);
    var token = adminToken();

    var createRequest =
        GradeCreateRequest.builder()
            .studentId(student.getId())
            .examId(exam.getId())
            .value(25.0)
            .build();
    var response =
        restTemplate.exchange(
            "/students/" + student.getId() + "/grades",
            POST,
            new HttpEntity<>(createRequest, headers(token)),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void student_can_read_own_grades_but_not_others() {
    var assignmentId = insertCourseAssignment();
    var exam = createExam(assignmentId, ExamType.REGULAR);
    var cohortId = insertCohort();
    var student = createStudent(cohortId);
    var otherStudent = createStudent(cohortId);
    var adminToken = adminToken();
    var createRequest =
        GradeCreateRequest.builder()
            .studentId(student.getId())
            .examId(exam.getId())
            .value(14.0)
            .build();
    restTemplate.exchange(
        "/students/" + student.getId() + "/grades",
        POST,
        new HttpEntity<>(createRequest, headers(adminToken)),
        Grade.class);

    var studentToken = login(userEmailOf(student.getUserId()), "test-password-only");

    var ownResponse =
        restTemplate.exchange(
            "/students/" + student.getId() + "/grades",
            GET,
            new HttpEntity<>(headers(studentToken)),
            new ParameterizedTypeReference<List<Grade>>() {});
    assertThat(ownResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(ownResponse.getBody()).hasSize(1);

    var othersResponse =
        restTemplate.exchange(
            "/students/" + otherStudent.getId() + "/grades",
            GET,
            new HttpEntity<>(headers(studentToken)),
            String.class);
    assertThat(othersResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void teacher_can_grade_student_they_teach_but_not_others() {
    var assignmentId = insertCourseAssignment();
    var exam = createExam(assignmentId, ExamType.REGULAR);
    var cohortId = insertCohort();
    var groupId = insertGroup(cohortId);
    var student = createStudent(cohortId);
    var otherStudent = createStudent(cohortId);
    jdbcTemplate.update(
        "INSERT INTO student_group_history (id, student_id, group_id, start_date, end_date)"
            + " VALUES (?, ?, ?, CURRENT_DATE, NULL)",
        randomUUID(),
        student.getId(),
        groupId);
    var teacher = createTeacher();
    courseAssignmentTeachingRepository.save(
        JCourseAssignmentTeaching.builder()
            .assignmentId(assignmentId)
            .teacherId(teacher.getId())
            .groupId(groupId)
            .build());
    var teacherToken = login(userEmailOf(teacher.getUserId()), "test-password-only");

    var okRequest =
        GradeCreateRequest.builder()
            .studentId(student.getId())
            .examId(exam.getId())
            .value(12.0)
            .build();
    var okResponse =
        restTemplate.exchange(
            "/students/" + student.getId() + "/grades",
            POST,
            new HttpEntity<>(okRequest, headers(teacherToken)),
            Grade.class);
    assertThat(okResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);

    var koRequest =
        GradeCreateRequest.builder()
            .studentId(otherStudent.getId())
            .examId(exam.getId())
            .value(12.0)
            .build();
    var koResponse =
        restTemplate.exchange(
            "/students/" + otherStudent.getId() + "/grades",
            POST,
            new HttpEntity<>(koRequest, headers(teacherToken)),
            String.class);
    assertThat(koResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void update_grade_requires_reason_and_creates_history_entry() {
    var assignmentId = insertCourseAssignment();
    var exam = createExam(assignmentId, ExamType.REGULAR);
    var cohortId = insertCohort();
    var student = createStudent(cohortId);
    var token = adminToken();
    var createRequest =
        GradeCreateRequest.builder()
            .studentId(student.getId())
            .examId(exam.getId())
            .value(9.0)
            .build();
    var created =
        restTemplate.exchange(
            "/students/" + student.getId() + "/grades",
            POST,
            new HttpEntity<>(createRequest, headers(token)),
            Grade.class);
    var gradeId = created.getBody().id();

    var missingReasonResponse =
        restTemplate.exchange(
            "/grades/" + gradeId,
            PUT,
            new HttpEntity<>(
                GradeUpdateRequest.builder().value(11.0).reason("").build(), headers(token)),
            String.class);
    assertThat(missingReasonResponse.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

    var updateResponse =
        restTemplate.exchange(
            "/grades/" + gradeId,
            PUT,
            new HttpEntity<>(
                GradeUpdateRequest.builder().value(11.0).reason("Grading error on Q3").build(),
                headers(token)),
            Grade.class);
    assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updateResponse.getBody().value()).isEqualTo(11.0);

    var historyResponse =
        restTemplate.exchange(
            "/grades/" + gradeId + "/history",
            GET,
            new HttpEntity<>(headers(token)),
            new ParameterizedTypeReference<List<GradeHistory>>() {});
    assertThat(historyResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(historyResponse.getBody()).hasSize(1);
    assertThat(historyResponse.getBody().get(0).reason()).isEqualTo("Grading error on Q3");
  }

  @Test
  void resit_grade_rejected_without_prior_regular_grade() {
    var assignmentId = insertCourseAssignment();
    var resitExam = createExam(assignmentId, ExamType.RESIT);
    var cohortId = insertCohort();
    var student = createStudent(cohortId);
    var token = adminToken();

    var request =
        GradeCreateRequest.builder()
            .studentId(student.getId())
            .examId(resitExam.getId())
            .value(12.0)
            .build();
    var response =
        restTemplate.exchange(
            "/students/" + student.getId() + "/grades",
            POST,
            new HttpEntity<>(request, headers(token)),
            String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void access_denied_without_token() {
    var response =
        restTemplate.exchange(
            "/students/" + randomUUID() + "/grades",
            GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
  }

  private String userEmailOf(UUID userId) {
    return jdbcTemplate.queryForObject(
        "SELECT email FROM \"user\" WHERE id = ?", String.class, userId);
  }
}
