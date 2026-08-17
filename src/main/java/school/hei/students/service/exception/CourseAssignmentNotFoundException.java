package school.hei.students.service.exception;

public class CourseAssignmentNotFoundException extends EntityNotFoundException {
  public CourseAssignmentNotFoundException(Object identifier) {
    super("CourseAssignment", identifier);
  }
}
