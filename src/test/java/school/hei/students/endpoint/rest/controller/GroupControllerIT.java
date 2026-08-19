package school.hei.students.endpoint.rest.controller;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.PUT;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import school.hei.students.conf.FacadeIT;
import school.hei.students.endpoint.rest.controller.dto.CohortCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.GroupCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.GroupUpdateRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.model.Cohort;
import school.hei.students.model.Group;
import school.hei.students.model.Role;
import school.hei.students.service.UserService;

class GroupControllerIT extends FacadeIT {
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
  void create_and_filter_group_by_cohort() {
    var token = loginAsAdmin();
    var cohortEntity = new HttpEntity<>(new CohortCreateRequest(2025), authHeaders(token));
    var cohort = restTemplate.postForEntity("/admin/cohorts", cohortEntity, Cohort.class).getBody();
    var groupEntity =
        new HttpEntity<>(new GroupCreateRequest("K1", null, cohort.id()), authHeaders(token));
    var group = restTemplate.postForEntity("/admin/groups", groupEntity, Group.class).getBody();
    assertThat(group.code()).isEqualTo("K1");
    var filterEntity = new HttpEntity<Void>(authHeaders(token));
    var filterResponse =
        restTemplate.exchange(
            "/admin/groups?cohortId=" + cohort.id(), GET, filterEntity, Group[].class);
    assertThat(filterResponse.getBody()).hasSize(1);
  }

  @Test
  void update_group_code_ok() {
    var token = loginAsAdmin();
    var cohortEntity = new HttpEntity<>(new CohortCreateRequest(2025), authHeaders(token));
    var cohort = restTemplate.postForEntity("/admin/cohorts", cohortEntity, Cohort.class).getBody();
    var groupEntity =
        new HttpEntity<>(new GroupCreateRequest("K2", null, cohort.id()), authHeaders(token));
    var group = restTemplate.postForEntity("/admin/groups", groupEntity, Group.class).getBody();
    var updateEntity = new HttpEntity<>(new GroupUpdateRequest("K3"), authHeaders(token));
    var updateResponse =
        restTemplate.exchange("/admin/groups/" + group.id(), PUT, updateEntity, Group.class);
    assertThat(updateResponse.getBody().code()).isEqualTo("K3");
  }
}
