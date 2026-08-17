package school.hei.students.mapper;

import java.util.List;
import org.springframework.stereotype.Component;
import school.hei.students.model.Track;
import school.hei.students.repository.model.JTrack;

@Component
public class TrackMapper {
  public Track toModel(JTrack entity) {
    return Track.builder().id(entity.getId()).name(entity.getName()).build();
  }

  public List<Track> toModel(List<JTrack> entities) {
    return entities.stream().map(this::toModel).toList();
  }

  public JTrack toEntity(Track model) {
    return JTrack.builder().id(model.id()).name(model.name()).build();
  }
}
