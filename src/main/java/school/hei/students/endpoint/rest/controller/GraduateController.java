package school.hei.students.endpoint.rest.controller;

import java.nio.file.Files;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import school.hei.students.service.GradeActorResolver;
import school.hei.students.service.GraduateExportService;

@RestController
@AllArgsConstructor
public class GraduateController {
  private static final MediaType XLSX_MEDIA_TYPE =
      MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

  private final GraduateExportService graduateExportService;
  private final GradeActorResolver actorResolver;

  @GetMapping("/cohorts/{cohortId}/graduates/export")
  @SneakyThrows
  public ResponseEntity<ByteArrayResource> exportGraduates(
      @PathVariable UUID cohortId, Authentication authentication) {
    var file = graduateExportService.export(cohortId, actorResolver.resolve(authentication));
    var bytes = Files.readAllBytes(file.toPath());
    file.delete();
    var disposition =
        ContentDisposition.attachment().filename("graduates-" + cohortId + ".xlsx").build();
    return ResponseEntity.ok()
        .contentType(XLSX_MEDIA_TYPE)
        .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
        .body(new ByteArrayResource(bytes));
  }
}
