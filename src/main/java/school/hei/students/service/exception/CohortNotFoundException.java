package school.hei.students.service.exception;

public class CohortNotFoundException extends EntityNotFoundException {
  public CohortNotFoundException(Object identifier) {
    super("Cohort", identifier);
  }
}
