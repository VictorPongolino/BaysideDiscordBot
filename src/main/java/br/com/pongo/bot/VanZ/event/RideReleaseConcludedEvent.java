package br.com.pongo.bot.VanZ.event;

import br.com.pongo.bot.VanZ.event.enums.ConclusionType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RideReleaseConcludedEvent {
    private final long oldOwner;
    private final long requestedByUserId;
    private final ConclusionType conclusionType;
}
