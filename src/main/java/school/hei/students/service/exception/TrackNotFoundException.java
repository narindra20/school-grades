package school.hei.students.service.exception;

public class TrackNotFoundException extends EntityNotFoundException {
  public TrackNotFoundException(Object identifier) {
    super("Track", identifier);
  }
}
