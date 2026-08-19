package school.hei.students.endpoint.rest.controller.dto;

import school.hei.students.model.Role;

public record UserUpdateRequest(String email, String password, Role role) {}
