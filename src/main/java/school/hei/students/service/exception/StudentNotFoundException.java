package school.hei.students.service.exception;

public class StudentNotFoundException extends EntityNotFoundException {
  public StudentNotFoundException(Object identifier) {
    super("Student", identifier);
  }
}
