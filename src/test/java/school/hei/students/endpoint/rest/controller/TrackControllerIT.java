package school.hei.students.endpoint.rest.controller;

import static java.util.UUID.randomUUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.DELETE;
import static org.springframework.http.HttpMethod.GET;
import static org.springframework.http.HttpMethod.POST;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import school.hei.students.conf.FacadeIT;
import school.hei.students.endpoint.rest.controller.dto.CourseCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.endpoint.rest.controller.dto.TrackCreateRequest;
import school.hei.students.model.Course;
import school.hei.students.model.Role;
import school.hei.students.model.Track;
import school.hei.students.service.UserService;

class TrackControllerIT extends FacadeIT {
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
  void create_track_and_link_course() {
    var token = loginAsAdmin();
    var trackEntity =
        new HttpEntity<>(new TrackCreateRequest("EL-" + randomUUID()), authHeaders(token));
    var track = restTemplate.postForEntity("/admin/tracks", trackEntity, Track.class).getBody();
    var courseEntity =
        new HttpEntity<>(
            new CourseCreateRequest("code-" + randomUUID(), "Algo", 5, "L1", "S1"),
            authHeaders(token));
    var course = restTemplate.postForEntity("/admin/courses", courseEntity, Course.class).getBody();
    var linkEntity = new HttpEntity<Void>(authHeaders(token));
    var linkResponse =
        restTemplate.exchange(
            "/admin/tracks/" + track.id() + "/courses/" + course.id(),
            POST,
            linkEntity,
            Void.class);
    assertThat(linkResponse.getStatusCode()).isEqualTo(HttpStatus.CREATED);
    var getEntity = new HttpEntity<Void>(authHeaders(token));
    var getResponse =
        restTemplate.exchange(
            "/admin/tracks/" + track.id() + "/courses", GET, getEntity, Course[].class);
    assertThat(getResponse.getBody()).hasSize(1);
  }

  @Test
  void unlink_course_from_track_ok() {
    var token = loginAsAdmin();
    var trackEntity =
        new HttpEntity<>(new TrackCreateRequest("ART-" + randomUUID()), authHeaders(token));
    var track = restTemplate.postForEntity("/admin/tracks", trackEntity, Track.class).getBody();
    var courseEntity =
        new HttpEntity<>(
            new CourseCreateRequest("code-" + randomUUID(), "Maths", 5, "L1", "S1"),
            authHeaders(token));
    var course = restTemplate.postForEntity("/admin/courses", courseEntity, Course.class).getBody();
    var linkEntity = new HttpEntity<Void>(authHeaders(token));
    restTemplate.exchange(
        "/admin/tracks/" + track.id() + "/courses/" + course.id(), POST, linkEntity, Void.class);
    var unlinkResponse =
        restTemplate.exchange(
            "/admin/tracks/" + track.id() + "/courses/" + course.id(),
            DELETE,
            linkEntity,
            Void.class);
    assertThat(unlinkResponse.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
  }

  @Test
  void delete_track_with_linked_course_ko() {
    var token = loginAsAdmin();
    var trackEntity =
        new HttpEntity<>(new TrackCreateRequest("SEC-" + randomUUID()), authHeaders(token));
    var track = restTemplate.postForEntity("/admin/tracks", trackEntity, Track.class).getBody();
    var courseEntity =
        new HttpEntity<>(
            new CourseCreateRequest("code-" + randomUUID(), "Physique", 4, "L2", "S1"),
            authHeaders(token));
    var course = restTemplate.postForEntity("/admin/courses", courseEntity, Course.class).getBody();
    var linkEntity = new HttpEntity<Void>(authHeaders(token));
    restTemplate.exchange(
        "/admin/tracks/" + track.id() + "/courses/" + course.id(), POST, linkEntity, Void.class);
    var deleteResponse =
        restTemplate.exchange("/admin/tracks/" + track.id(), DELETE, linkEntity, String.class);
    assertThat(deleteResponse.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
  }
}
