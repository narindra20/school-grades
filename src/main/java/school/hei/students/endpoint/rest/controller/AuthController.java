package school.hei.students.endpoint.rest.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.endpoint.rest.controller.dto.LoginRequest;
import school.hei.students.endpoint.rest.controller.dto.LoginResponse;
import school.hei.students.security.JwtUtil;
import school.hei.students.service.UserNotFoundException;
import school.hei.students.service.UserService;

@RestController
@RequestMapping("/auth")
@AllArgsConstructor
public class AuthController {
  private final UserService userService;
  private final JwtUtil jwtUtil;
  private final PasswordEncoder passwordEncoder;

  @PostMapping("/login")
  public LoginResponse login(@RequestBody LoginRequest request) {
    var user = fetchUser(request.email());
    if (!user.active() || !passwordEncoder.matches(request.password(), user.password())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
    var token = jwtUtil.generateToken(user.email(), user.role().name());
    return new LoginResponse(token, user.role());
  }

  private school.hei.students.model.User fetchUser(String email) {
    try {
      return userService.getByEmail(email);
    } catch (UserNotFoundException e) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
    }
  }
}
