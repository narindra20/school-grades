package school.hei.students.endpoint.rest.controller;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import school.hei.students.conf.FacadeIT;
import school.hei.students.endpoint.event.EventProducer;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.endpoint.rest.controller.dto.TranscriptSendRequest;
import school.hei.students.model.DeliveryStatus;
import school.hei.students.model.Role;
import school.hei.students.model.TranscriptDelivery;
import school.hei.students.repository.StudentRepository;
import school.hei.students.repository.TranscriptDeliveryRepository;
import school.hei.students.repository.model.JStudent;
import school.hei.students.service.UserService;

class TranscriptControllerIT extends FacadeIT {

  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserService userService;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private StudentRepository studentRepository;
  @Autowired private TranscriptDeliveryRepository transcriptDeliveryRepository;

  @MockBean private EventProducer eventProducer;

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

  private UUID insertCohort() {
    var cohortId = randomUUID();
    jdbcTemplate.update("INSERT INTO cohort (id, entry_year) VALUES (?, ?)", cohortId, 2025);
    return cohortId;
  }

  private JStudent createStudent(UUID cohortId) {
    var uniqueSuffix = randomUUID();
    var email = "transcript-it-student-" + uniqueSuffix + "@hei.school";

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

  private String adminToken() {
    var email = "transcript-it-admin-" + randomUUID() + "@hei.school";

    userService.create(randomUUID(), email, "test-password-only", Role.ADMIN);

    return login(email, "test-password-only");
  }

  private String userEmailOf(UUID userId) {
    return jdbcTemplate.queryForObject(
        "SELECT email FROM \"user\" WHERE id = ?", String.class, userId);
  }

  private void insertDelivery(UUID studentId, String academicYear, DeliveryStatus status) {

    var sentAt = status == DeliveryStatus.SENT ? Timestamp.from(java.time.Instant.now()) : null;

    jdbcTemplate.update(
        "INSERT INTO transcript_delivery "
            + "(id, student_id, academic_year, sent_at, s3_url, status) "
            + "VALUES (?, ?, ?, ?, ?, ?)",
        randomUUID(),
        studentId,
        academicYear,
        sentAt,
        status == DeliveryStatus.SENT ? "transcripts/dummy.pdf" : null,
        status.name());
  }

  @Test
  void admin_requests_transcript_creates_in_progress_delivery_and_fires_event() {
    var cohortId = insertCohort();
    var student = createStudent(cohortId);
    var token = adminToken();

    var response =
        restTemplate.exchange(
            "/students/" + student.getId() + "/transcript/send",
            POST,
            new HttpEntity<>(new TranscriptSendRequest("2025-2026"), headers(token)),
            TranscriptDelivery.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    assertThat(response.getBody().studentId()).isEqualTo(student.getId());
    assertThat(response.getBody().academicYear()).isEqualTo("2025-2026");
    assertThat(response.getBody().status()).isEqualTo(DeliveryStatus.IN_PROGRESS);
    assertThat(response.getBody().sentAt()).isNull();

    var persisted = transcriptDeliveryRepository.findById(response.getBody().id());

    assertThat(persisted).isPresent();
    assertThat(persisted.get().getStatus()).isEqualTo(DeliveryStatus.IN_PROGRESS.name());

    verify(eventProducer).accept(any());
  }

  @Test
  void request_transcript_rejects_missing_academic_year() {
    var cohortId = insertCohort();
    var student = createStudent(cohortId);
    var token = adminToken();

    var response =
        restTemplate.exchange(
            "/students/" + student.getId() + "/transcript/send",
            POST,
            new HttpEntity<>(new TranscriptSendRequest(""), headers(token)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
  }

  @Test
  void request_transcript_for_unknown_student_returns_not_found() {
    var token = adminToken();

    var response =
        restTemplate.exchange(
            "/students/" + randomUUID() + "/transcript/send",
            POST,
            new HttpEntity<>(new TranscriptSendRequest("2025-2026"), headers(token)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }

  @Test
  void student_can_request_own_transcript_but_not_a_classmates() {
    var cohortId = insertCohort();
    var student = createStudent(cohortId);
    var otherStudent = createStudent(cohortId);

    var studentToken = login(userEmailOf(student.getUserId()), "test-password-only");

    var ownResponse =
        restTemplate.exchange(
            "/students/" + student.getId() + "/transcript/send",
            POST,
            new HttpEntity<>(new TranscriptSendRequest("2025-2026"), headers(studentToken)),
            TranscriptDelivery.class);

    assertThat(ownResponse.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);

    var othersResponse =
        restTemplate.exchange(
            "/students/" + otherStudent.getId() + "/transcript/send",
            POST,
            new HttpEntity<>(new TranscriptSendRequest("2025-2026"), headers(studentToken)),
            String.class);

    assertThat(othersResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void student_reads_own_delivery_history() {
    var cohortId = insertCohort();
    var student = createStudent(cohortId);

    insertDelivery(student.getId(), "2023-2024", DeliveryStatus.SENT);

    insertDelivery(student.getId(), "2024-2025", DeliveryStatus.FAILED);

    var studentToken = login(userEmailOf(student.getUserId()), "test-password-only");

    var response =
        restTemplate.exchange(
            "/students/" + student.getId() + "/transcript-deliveries",
            GET,
            new HttpEntity<>(headers(studentToken)),
            new ParameterizedTypeReference<List<TranscriptDelivery>>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).hasSize(2);
  }

  @Test
  void admin_reads_global_delivery_history() {
    var cohortId = insertCohort();
    var student = createStudent(cohortId);

    insertDelivery(student.getId(), "2023-2024", DeliveryStatus.SENT);

    var token = adminToken();

    var response =
        restTemplate.exchange(
            "/admin/transcript-deliveries",
            GET,
            new HttpEntity<>(headers(token)),
            new ParameterizedTypeReference<List<TranscriptDelivery>>() {});

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).isNotEmpty();
  }

  @Test
  void non_admin_cannot_read_global_delivery_history() {
    var cohortId = insertCohort();
    var student = createStudent(cohortId);

    var studentToken = login(userEmailOf(student.getUserId()), "test-password-only");

    var response =
        restTemplate.exchange(
            "/admin/transcript-deliveries",
            GET,
            new HttpEntity<>(headers(studentToken)),
            String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void access_denied_without_token() {
    var response =
        restTemplate.exchange(
            "/students/" + randomUUID() + "/transcript-deliveries",
            GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class);

    assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
  }
}
