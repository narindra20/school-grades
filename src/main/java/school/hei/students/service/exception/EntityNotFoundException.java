package school.hei.students.service.exception;

public abstract class EntityNotFoundException extends RuntimeException {
  protected EntityNotFoundException(String entityName, Object identifier) {
    super(entityName + " not found: " + identifier);
  }
}
