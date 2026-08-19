package school.hei.students.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.students.endpoint.rest.controller.dto.TranscriptSendRequest;
import school.hei.students.model.TranscriptDelivery;
import school.hei.students.service.GradeActorResolver;
import school.hei.students.service.TranscriptDeliveryService;

@RestController
@AllArgsConstructor
public class TranscriptController {
  private final TranscriptDeliveryService transcriptDeliveryService;
  private final GradeActorResolver actorResolver;

  @PostMapping("/students/{studentId}/transcript/send")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public TranscriptDelivery sendTranscript(
      @PathVariable UUID studentId,
      @RequestBody TranscriptSendRequest request,
      Authentication authentication) {
    return transcriptDeliveryService.requestTranscript(
        studentId, request.academicYear(), actorResolver.resolve(authentication));
  }

  @GetMapping("/students/{studentId}/transcript-deliveries")
  public List<TranscriptDelivery> getDeliveryHistory(
      @PathVariable UUID studentId, Authentication authentication) {
    return transcriptDeliveryService.getHistoryForStudent(
        studentId, actorResolver.resolve(authentication));
  }

  @GetMapping("/admin/transcript-deliveries")
  public List<TranscriptDelivery> getAllDeliveryHistory() {
    return transcriptDeliveryService.getAllHistory();
  }
}
