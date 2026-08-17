package school.hei.students.service.exception;

public class GroupNotFoundException extends EntityNotFoundException {
  public GroupNotFoundException(Object identifier) {
    super("Group", identifier);
  }
}
