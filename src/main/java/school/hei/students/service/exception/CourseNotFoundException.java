package school.hei.students.service.exception;

public class CourseNotFoundException extends EntityNotFoundException {
  public CourseNotFoundException(Object identifier) {
    super("Course", identifier);
  }
}
