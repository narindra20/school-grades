package school.hei.students.service.exception;

public class TranscriptDeliveryNotFoundException extends EntityNotFoundException {
  public TranscriptDeliveryNotFoundException(Object identifier) {
    super("TranscriptDelivery", identifier);
  }
}
