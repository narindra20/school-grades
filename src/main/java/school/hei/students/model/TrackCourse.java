package school.hei.students.model;

import java.util.UUID;
import lombok.Builder;

@Builder
public record TrackCourse(UUID trackId, UUID courseId) {}
