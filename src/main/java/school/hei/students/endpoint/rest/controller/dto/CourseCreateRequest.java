package school.hei.students.endpoint.rest.controller.dto;

public record CourseCreateRequest(
    String code, String title, Integer credits, String level, String semester) {}
