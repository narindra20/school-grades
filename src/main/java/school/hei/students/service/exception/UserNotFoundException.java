package school.hei.students.service.exception;

public class UserNotFoundException extends EntityNotFoundException {
  public UserNotFoundException(Object identifier) {
    super("User", identifier);
  }
}
