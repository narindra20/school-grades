package school.hei.students.model;

import java.time.Instant;
import java.util.UUID;
import lombok.Builder;

@Builder
public record TranscriptDelivery(
    UUID id, UUID studentId, String academicYear, Instant sentAt, String s3Url, String status) {}
