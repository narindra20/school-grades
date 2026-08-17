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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import school.hei.students.endpoint.rest.controller.dto.GroupCreateRequest;
import school.hei.students.endpoint.rest.controller.dto.GroupUpdateRequest;
import school.hei.students.model.Group;
import school.hei.students.service.GroupService;

@RestController
@RequestMapping("/admin/groups")
@AllArgsConstructor
public class GroupController {
  private final GroupService service;

  @GetMapping
  public List<Group> getAll(
      @RequestParam(required = false) UUID cohortId, @RequestParam(required = false) UUID trackId) {
    return service.getAll(cohortId, trackId);
  }

  @GetMapping("/{groupId}")
  public Group getById(@PathVariable UUID groupId) {
    return service.getById(groupId);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Group create(@RequestBody GroupCreateRequest request) {
    return service.create(request.code(), request.trackId(), request.cohortId());
  }

  @PutMapping("/{groupId}")
  public Group update(@PathVariable UUID groupId, @RequestBody GroupUpdateRequest request) {
    return service.updateCode(groupId, request.code());
  }

  @DeleteMapping("/{groupId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(@PathVariable UUID groupId) {
    service.delete(groupId);
  }
}
