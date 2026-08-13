package school.hei.students.endpoint.event.consumer.model;

import school.hei.students.PojaGenerated;
import school.hei.students.endpoint.event.model.PojaEvent;

@PojaGenerated
public record TypedEvent(String typeName, PojaEvent payload) {}
