package school.hei.students.service;

import static java.util.UUID.randomUUID;
import static org.springframework.http.HttpStatus.CONFLICT;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import school.hei.students.mapper.CourseMapper;
import school.hei.students.mapper.TrackMapper;
import school.hei.students.model.Course;
import school.hei.students.model.Track;
import school.hei.students.repository.CourseRepository;
import school.hei.students.repository.GroupRepository;
import school.hei.students.repository.TrackCourseRepository;
import school.hei.students.repository.TrackRepository;
import school.hei.students.repository.model.JTrackCourse;
import school.hei.students.repository.model.JTrackCourseId;
import school.hei.students.service.exception.CourseNotFoundException;
import school.hei.students.service.exception.TrackCourseLinkNotFoundException;
import school.hei.students.service.exception.TrackNotFoundException;

@Service
@AllArgsConstructor
public class TrackService {
  private final TrackRepository repository;
  private final TrackMapper mapper;
  private final GroupRepository groupRepository;
  private final TrackCourseRepository trackCourseRepository;
  private final CourseRepository courseRepository;
  private final CourseMapper courseMapper;

  public List<Track> getAll() {
    return mapper.toModel(repository.findAll());
  }

  public Track getById(UUID id) {
    return mapper.toModel(
        repository.findById(id).orElseThrow(() -> new TrackNotFoundException(id)));
  }

  public Track create(String name) {
    var toSave = Track.builder().id(randomUUID()).name(name).build();
    return mapper.toModel(repository.save(mapper.toEntity(toSave)));
  }

  public Track update(UUID id, String name) {
    var existing = getById(id);
    var updated =
        Track.builder().id(existing.id()).name(name != null ? name : existing.name()).build();
    return mapper.toModel(repository.save(mapper.toEntity(updated)));
  }

  public void delete(UUID id) {
    getById(id);
    var hasGroups = !groupRepository.findByTrackId(id).isEmpty();
    var hasCourses = !trackCourseRepository.findByIdTrackId(id).isEmpty();
    if (hasGroups || hasCourses) {
      throw new ResponseStatusException(CONFLICT, "Groups or courses are linked to this track");
    }
    repository.deleteById(id);
  }

  public List<Course> getCourses(UUID trackId) {
    getById(trackId);
    return trackCourseRepository.findByIdTrackId(trackId).stream()
        .map(
            jtc ->
                courseRepository
                    .findById(jtc.getId().getCourseId())
                    .orElseThrow(() -> new CourseNotFoundException(jtc.getId().getCourseId())))
        .map(courseMapper::toModel)
        .toList();
  }

  public void linkCourse(UUID trackId, UUID courseId) {
    getById(trackId);
    if (courseRepository.findById(courseId).isEmpty()) {
      throw new CourseNotFoundException(courseId);
    }
    var id = JTrackCourseId.builder().trackId(trackId).courseId(courseId).build();
    trackCourseRepository.save(JTrackCourse.builder().id(id).build());
  }

  public void unlinkCourse(UUID trackId, UUID courseId) {
    var id = JTrackCourseId.builder().trackId(trackId).courseId(courseId).build();
    if (trackCourseRepository.findById(id).isEmpty()) {
      throw new TrackCourseLinkNotFoundException(trackId + "/" + courseId);
    }
    trackCourseRepository.deleteById(id);
  }
}
