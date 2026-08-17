package school.hei.students.endpoint.rest.controller;

import java.util.List;
import java.util.UUID;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.students.endpoint.rest.controller.dto.CohortCreateRequest;
import school.hei.students.model.Cohort;
import school.hei.students.service.CohortService;

@RestController
@AllArgsConstructor
public class CohortController {
  private final CohortService service;

  @GetMapping("/cohorts")
  public List<Cohort> getAll() {
    return service.getAll();
  }

  @GetMapping("/admin/cohorts/{cohortId}")
  public Cohort getById(@PathVariable UUID cohortId) {
    return service.getById(cohortId);
  }

  @PostMapping("/admin/cohorts")
  @ResponseStatus(HttpStatus.CREATED)
  public Cohort create(@RequestBody CohortCreateRequest request) {
    return service.create(request.entryYear());
  }

  @PutMapping("/admin/cohorts/{cohortId}")
  public Cohort update(@PathVariable UUID cohortId, @RequestBody CohortCreateRequest request) {
    return service.update(cohortId, request.entryYear());
  }

  @DeleteMapping("/admin/cohorts/{cohortId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID cohortId) {
    service.delete(cohortId);
  }
}
