package school.hei.students.endpoint.rest.controller;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import school.hei.students.conf.FacadeIT;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.model.Role;
import school.hei.students.repository.CohortRepository;
import school.hei.students.repository.GroupRepository;
import school.hei.students.repository.model.JCohort;
import school.hei.students.repository.model.JGroup;
import school.hei.students.service.UserService;

class WebControllerIT extends FacadeIT {
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserService userService;
  @Autowired private CohortRepository cohortRepository;
  @Autowired private GroupRepository groupRepository;

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
    var email = "web-it-admin-" + randomUUID() + "@hei.school";
    userService.create(randomUUID(), email, "test-password-only", Role.ADMIN);
    return login(email, "test-password-only");
  }

  private String tokenWithRole(Role role) {
    var email = "web-it-" + role.name().toLowerCase() + "-" + randomUUID() + "@hei.school";
    userService.create(randomUUID(), email, "test-password-only", role);
    return login(email, "test-password-only");
  }

  @Test
  void authenticated_user_sees_cohort_list_and_download_link() {
    var cohort = cohortRepository.save(JCohort.builder().entryYear(2025).build());
    var group = groupRepository.save(JGroup.builder().code("K1").cohortId(cohort.getId()).build());
    var token = adminToken();

    var response =
        restTemplate.exchange("/web/cohorts", GET, new HttpEntity<>(headers(token)), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getHeaders().getContentType())
        .isEqualTo(MediaType.valueOf("text/html;charset=UTF-8"));
    assertThat(response.getBody()).contains("2025");
    assertThat(response.getBody()).contains(group.getCode());
    assertThat(response.getBody()).contains("/cohorts/" + cohort.getId() + "/graduates/export");
  }

  @Test
  void cohort_without_group_shows_empty_group_cell() {
    var cohort = cohortRepository.save(JCohort.builder().entryYear(2027).build());
    var token = adminToken();

    var response =
        restTemplate.exchange("/web/cohorts", GET, new HttpEntity<>(headers(token)), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(response.getBody()).contains("2027");
  }

  @Test
  void access_denied_without_token() {
    var response =
        restTemplate.exchange(
            "/web/cohorts", GET, new HttpEntity<>(new HttpHeaders()), String.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
  }

  @Test
  void token_query_param_works_for_direct_browser_navigation() {
    var cohort = cohortRepository.save(JCohort.builder().entryYear(2026).build());
    var token = adminToken();

    var pageResponse =
        restTemplate.exchange(
            "/web/cohorts?token=" + token, GET, new HttpEntity<>(new HttpHeaders()), String.class);
    assertThat(pageResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(pageResponse.getBody()).contains("token=" + token);

    var downloadResponse =
        restTemplate.exchange(
            "/cohorts/" + cohort.getId() + "/graduates/export?token=" + token,
            GET,
            new HttpEntity<>(new HttpHeaders()),
            byte[].class);
    assertThat(downloadResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void invalid_token_query_param_is_rejected() {
    var response =
        restTemplate.exchange(
            "/web/cohorts?token=not-a-real-token",
            GET,
            new HttpEntity<>(new HttpHeaders()),
            String.class);
    assertThat(response.getStatusCode()).isIn(HttpStatus.UNAUTHORIZED, HttpStatus.FORBIDDEN);
  }

  @Test
  void student_cannot_see_cohort_list() {
    var token = tokenWithRole(Role.STUDENT);

    var response =
        restTemplate.exchange("/web/cohorts", GET, new HttpEntity<>(headers(token)), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void teacher_cannot_see_cohort_list() {
    var token = tokenWithRole(Role.TEACHER);

    var response =
        restTemplate.exchange("/web/cohorts", GET, new HttpEntity<>(headers(token)), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }

  @Test
  void non_admin_token_query_param_is_rejected() {
    var token = tokenWithRole(Role.STUDENT);

    var response =
        restTemplate.exchange(
            "/web/cohorts?token=" + token, GET, new HttpEntity<>(new HttpHeaders()), String.class);

    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
  }
}
