package school.hei.students.endpoint.rest.controller;

import static java.util.UUID.randomUUID;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.students.endpoint.rest.controller.dto.UserCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.UserResponse;
import school.hei.students.endpoint.rest.controller.dto.UserUpdateRequest;
import school.hei.students.service.UserService;

@RestController
@RequestMapping("/admin/users")
@AllArgsConstructor
public class UserController {
  private final UserService userService;

  @GetMapping
  public List<UserResponse> getAll() {
    return userService.getAll().stream().map(UserResponse::from).toList();
  }

  @GetMapping("/{userId}")
  public UserResponse getById(@PathVariable UUID userId) {
    return UserResponse.from(userService.getById(userId));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public UserResponse create(@RequestBody UserCreateRequest request) {
    return UserResponse.from(
        userService.create(randomUUID(), request.email(), request.password(), request.role()));
  }

  @PutMapping("/{userId}")
  public UserResponse update(@PathVariable UUID userId, @RequestBody UserUpdateRequest request) {
    return UserResponse.from(
        userService.update(userId, request.email(), request.password(), request.role()));
  }

  @DeleteMapping("/{userId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deactivate(@PathVariable UUID userId) {
    userService.deactivate(userId);
  }
}
