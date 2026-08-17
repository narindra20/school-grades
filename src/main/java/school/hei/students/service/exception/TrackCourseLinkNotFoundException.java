package school.hei.students.service.exception;

public class TrackCourseLinkNotFoundException extends EntityNotFoundException {
  public TrackCourseLinkNotFoundException(Object identifier) {
    super("TrackCourseLink", identifier);
  }
}
