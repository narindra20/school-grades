package school.hei.students.service.exception;

public class CourseAssignmentTeachingNotFoundException extends EntityNotFoundException {
  public CourseAssignmentTeachingNotFoundException(Object identifier) {
    super("CourseAssignmentTeaching", identifier);
  }
}
