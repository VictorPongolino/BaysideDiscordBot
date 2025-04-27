package br.com.pongo.bot.VanZ.event;

import br.com.pongo.bot.VanZ.event.enums.ConclusionType;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class RideReleaseRequestedEvent {
    private final long ownerId;
    private final long requestedByUserId;
    private final ConclusionType conclusionType;
}

