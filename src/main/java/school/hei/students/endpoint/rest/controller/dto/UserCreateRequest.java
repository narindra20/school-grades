package school.hei.students.endpoint.rest.controller.dto;

import school.hei.students.model.Role;

public record UserCreateRequest(String email, String password, Role role) {}
