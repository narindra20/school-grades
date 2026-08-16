package school.hei.students.service;

public class UserNotFoundException extends RuntimeException {
  public UserNotFoundException(Object identifier) {
    super("User not found: " + identifier);
  }
}
