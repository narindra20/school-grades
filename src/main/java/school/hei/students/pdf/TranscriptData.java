package school.hei.students.pdf;

import java.util.List;
import lombok.Builder;

@Builder
public record TranscriptData(
    String studentFirstName,
    String studentLastName,
    String studentNumber,
    String academicYear,
    List<TranscriptCourseLine> lines,
    Double overallAverage) {}
