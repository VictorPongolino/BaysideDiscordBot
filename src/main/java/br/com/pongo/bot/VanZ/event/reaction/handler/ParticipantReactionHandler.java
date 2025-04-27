package br.com.pongo.bot.VanZ.event.reaction.handler;

import br.com.pongo.bot.VanZ.domain.CompanyVehicle.Passenger;
import br.com.pongo.bot.VanZ.domain.CompanyVehicle.Passenger.MeetUpPreference;
import br.com.pongo.bot.VanZ.event.reaction.ReactionInteractionHandler;
import br.com.pongo.bot.VanZ.service.PlayerNotificationService;
import br.com.pongo.bot.VanZ.service.VehicleStateService;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Objects;
import java.util.Optional;
import static br.com.pongo.bot.VanZ.domain.CompanyVehicle.Passenger.MeetUpPreference.*;


@Component
@Qualifier("participant")
@RequiredArgsConstructor
public class ParticipantReactionHandler implements ReactionInteractionHandler {

    private final VehicleStateService vehicleStateService;
    private final PlayerNotificationService playerNotificationService;

    @Override
    public Mono<Void> doOnsiteInteractionHandler(final ReactionAddEvent event) {
        long userId = event.getUserId().asLong();
        if (hasPlayerReactedPreviouslyWith(userId, MeetUpPreference.ONSITE)) {
            return Mono.empty();
        }

        vehicleStateService.addPassengerOrUpdateToOnsite(userId);

        return sendSingleMessage(event, "<@%d> escolheu o seu ponto de encontro como sendo a empresa !".formatted(userId));
    }

    @Override
    public Mono<Void> doBaysideInteractionHandler(final ReactionAddEvent event) {
        long userIdentifier = event.getUserId().asLong();
        if (hasPlayerReactedPreviouslyWith(userIdentifier, BAYSIDE)) {
            return Mono.empty();
        }

        vehicleStateService.addPassengerToBayside(event.getUserId().asLong());
        return sendSingleMessage(event, "<@%d> escolheu o seu ponto de encontro como sendo Bayside !".formatted(userIdentifier));
    }

    @Override
    public Mono<Void> doNoneInteractionHandler(final ReactionAddEvent event) {
        long userIdentifier = event.getUserId().asLong();
        if (hasPlayerReactedPreviouslyWith(userIdentifier, NONE)) {
            return Mono.empty();
        }

        var optionalPassenger = vehicleStateService.getCompanyVehicle().getPassengerByIdentifier(userIdentifier);
        if (optionalPassenger.isPresent() && !optionalPassenger.get().getMeetUpPreference().equals(NONE)) {
            return sendSingleMessage(event, "<@%d> mudou de ideia e optou de não ir.".formatted(userIdentifier));
        }

        vehicleStateService.addPassengerToNone(userIdentifier);
        return Mono.empty();
    }

    private boolean hasPlayerReactedPreviouslyWith(final long identifier, final MeetUpPreference meetUpPreference) {
        Optional<Passenger> optionalPassenger = vehicleStateService.getCompanyVehicle()
                .getPassengerByIdentifier(identifier);

        if (optionalPassenger.isPresent()) {
            MeetUpPreference currentPreference = optionalPassenger.get().getMeetUpPreference();
            return Objects.nonNull(currentPreference) && currentPreference.equals(meetUpPreference);
        }

        return false;
    }

    private Mono<Void> sendSingleMessage(ReactionAddEvent event, String chatNotification) {
        return event.getMessage()
                .flatMap(Message::getChannel)
                .flatMap(messageChannel ->
                        playerNotificationService.sendSingleMessage(messageChannel, chatNotification, event.getUserId().asLong())
                ).then();
    }
}
