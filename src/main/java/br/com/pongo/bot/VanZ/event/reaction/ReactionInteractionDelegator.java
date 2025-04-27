package br.com.pongo.bot.VanZ.event.reaction;

import br.com.pongo.bot.VanZ.domain.VehicleInteractionConfig;
import br.com.pongo.bot.VanZ.service.VehicleStateService;
import discord4j.core.event.domain.message.ReactionAddEvent;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Map;
import java.util.function.BiFunction;

@Component
public class ReactionInteractionDelegator {

    private final ReactionInteractionHandler ownerHandler;
    private final ReactionInteractionHandler participantHandler;
    private final VehicleStateService vehicleStateService;
    private final VehicleInteractionConfig vehicleInteractionConfig;

    private final Map<VehicleInteractionConfig.MeetingDetails, BiFunction<ReactionInteractionHandler, ReactionAddEvent, Mono<Void>>> handlerMap;

    public ReactionInteractionDelegator(
            @Qualifier("owner") ReactionInteractionHandler ownerHandler,
            @Qualifier("participant") ReactionInteractionHandler participantHandler,
            VehicleStateService vehicleStateService,
            VehicleInteractionConfig vehicleInteractionConfig) {
        this.ownerHandler = ownerHandler;
        this.participantHandler = participantHandler;
        this.vehicleStateService = vehicleStateService;
        this.vehicleInteractionConfig = vehicleInteractionConfig;

        handlerMap = Map.of(
                vehicleInteractionConfig.getOnsiteMeeting(), ReactionInteractionHandler::doOnsiteInteractionHandler,
                vehicleInteractionConfig.getBaysideMeeting(), ReactionInteractionHandler::doBaysideInteractionHandler,
                vehicleInteractionConfig.getNoneMeeting(), ReactionInteractionHandler::doNoneInteractionHandler
                );
    }

    public Mono<Void> delegate(final long userIdentifier, final ReactionAddEvent event) {
        var meetingDetails = vehicleInteractionConfig
                .getMeetingDetailsForEmoji(event.getEmoji().asUnicodeEmoji().get().getRaw())
                .orElseThrow();

        var handler = vehicleStateService.getCompanyVehicle().getOwnerId() == userIdentifier ? ownerHandler : participantHandler;
        return handlerMap.get(meetingDetails).apply(handler, event);
    }
}
