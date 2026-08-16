package school.hei.students.endpoint.rest.controller.dto;

import school.hei.students.model.Role;

public record LoginResponse(String token, Role role) {}
