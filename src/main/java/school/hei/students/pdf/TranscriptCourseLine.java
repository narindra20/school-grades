package school.hei.students.pdf;

import lombok.Builder;

@Builder
public record TranscriptCourseLine(
    String courseCode, String courseTitle, Integer credits, Double average) {}
