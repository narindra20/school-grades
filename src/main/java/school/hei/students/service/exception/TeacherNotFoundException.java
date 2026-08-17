package school.hei.students.service.exception;

public class TeacherNotFoundException extends EntityNotFoundException {
  public TeacherNotFoundException(Object identifier) {
    super("Teacher", identifier);
  }
}
