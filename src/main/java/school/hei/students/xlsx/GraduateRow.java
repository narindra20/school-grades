package school.hei.students.xlsx;

import lombok.Builder;

@Builder
public record GraduateRow(
    Integer rank, String studentNumber, String lastName, String firstName, Double overallAverage) {}
