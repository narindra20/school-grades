package school.hei.students.endpoint.rest.controller;

import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import school.hei.students.model.Cohort;
import school.hei.students.model.Group;
import school.hei.students.service.CohortService;
import school.hei.students.service.GroupService;

@Controller
@AllArgsConstructor
public class WebController {
  private final CohortService cohortService;
  private final GroupService groupService;

  @GetMapping("/web/cohorts")
  public String cohorts(@RequestParam(required = false) String token, Model model) {
    var cohorts = cohortService.getAll();
    Map<UUID, String> groupsByCohortId =
        cohorts.stream()
            .collect(
                Collectors.toMap(
                    Cohort::id,
                    cohort ->
                        groupService.getAll(cohort.id(), null).stream()
                            .map(Group::code)
                            .collect(Collectors.joining(", "))));
    model.addAttribute("cohorts", cohorts);
    model.addAttribute("groupsByCohortId", groupsByCohortId);
    model.addAttribute("token", token);
    return "cohorts";
  }
}
