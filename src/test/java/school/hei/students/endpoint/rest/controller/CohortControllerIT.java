package school.hei.students.endpoint.rest.controller;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import school.hei.students.conf.FacadeIT;
import school.hei.students.endpoint.rest.controller.dto.CohortCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.model.Cohort;
import school.hei.students.model.Role;
import school.hei.students.service.UserService;

class CohortControllerIT extends FacadeIT {
  @Autowired private TestRestTemplate restTemplate;
  @Autowired private UserService userService;

  private String loginAsAdmin() {
    var email = "admin-" + randomUUID() + "@hei.school";
    userService.create(randomUUID(), email, "password123", Role.ADMIN);
    var response =
        restTemplate.postForEntity(
            "/auth/login", new LoginRequest(email, "password123"), LoginResponse.class);
    return response.getBody().token();
  }

  private HttpHeaders authHeaders(String token) {
    var headers = new HttpHeaders();
    headers.setBearerAuth(token);
    return headers;
  }

  @Test
  void create_and_get_cohort() {
    var token = loginAsAdmin();
    var createEntity = new HttpEntity<>(new CohortCreateRequest(2025), authHeaders(token));
    var createResponse = restTemplate.postForEntity("/admin/cohorts", createEntity, Cohort.class);
    assertThat(createResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    assertThat(createResponse.getBody().entryYear()).isEqualTo(2025);
    var getEntity = new HttpEntity<Void>(authHeaders(token));
    var getResponse =
        restTemplate.exchange(
            "/admin/cohorts/" + createResponse.getBody().id(), GET, getEntity, Cohort.class);
    assertThat(getResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
  }

  @Test
  void update_cohort_ok() {
    var token = loginAsAdmin();
    var createEntity = new HttpEntity<>(new CohortCreateRequest(2024), authHeaders(token));
    var created =
        restTemplate.postForEntity("/admin/cohorts", createEntity, Cohort.class).getBody();
    var updateEntity = new HttpEntity<>(new CohortCreateRequest(2026), authHeaders(token));
    var updateResponse =
        restTemplate.exchange("/admin/cohorts/" + created.id(), PUT, updateEntity, Cohort.class);
    assertThat(updateResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
    assertThat(updateResponse.getBody().entryYear()).isEqualTo(2026);
  }

  @Test
  void delete_cohort_without_links_ok() {
    var token = loginAsAdmin();
    var createEntity = new HttpEntity<>(new CohortCreateRequest(2023), authHeaders(token));
    var created =
        restTemplate.postForEntity("/admin/cohorts", createEntity, Cohort.class).getBody();
    var deleteEntity = new HttpEntity<Void>(authHeaders(token));
    var deleteResponse =
        restTemplate.exchange("/admin/cohorts/" + created.id(), DELETE, deleteEntity, Void.class);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  void get_unknown_cohort_ko() {
    var token = loginAsAdmin();
    var getEntity = new HttpEntity<Void>(authHeaders(token));
    var response =
        restTemplate.exchange("/admin/cohorts/" + randomUUID(), GET, getEntity, String.class);
    assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
  }
}
