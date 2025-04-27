package br.com.pongo.bot.VanZ.event.reaction.handler;

import br.com.pongo.bot.VanZ.domain.CompanyVehicle;
import br.com.pongo.bot.VanZ.event.reaction.ReactionInteractionHandler;
import br.com.pongo.bot.VanZ.service.PlayerNotificationService;
import br.com.pongo.bot.VanZ.service.VehicleStateService;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static br.com.pongo.bot.VanZ.domain.CompanyVehicle.OwnerMeetUpPlace.*;

@Log4j2
@Component
@Qualifier("owner")
@RequiredArgsConstructor
public class OwnerReactionHandler implements ReactionInteractionHandler {

    private final VehicleStateService vehicleStateService;
    private final PlayerNotificationService playerNotificationService;

    @Override
    public Mono<Void> doOnsiteInteractionHandler(final ReactionAddEvent event) {
        log.info("Previous state {}", vehicleStateService.getCompanyVehicle().getOwnerMeetUpPlace());
        if (vehicleStateService.getCompanyVehicle().getOwnerMeetUpPlace().equals(ONSITE)) {
            return Mono.empty();
        }

        String chatNotification;
        if (!vehicleStateService.getCompanyVehicle().getOwnerMeetUpPlace().equals(DEFAULT)) {
            chatNotification = "<@%d> mudou de ideia e definiu o ponto de encontro para a empresa!".formatted(event.getUserId().asLong());
        } else {
            chatNotification = "<@%d> confirmou o ponto de encontro sendo a empresa como local de partida !".formatted(event.getUserId().asLong());
        }
        chatNotification += "\n> Demais usuários podem interagir para definirem aonde deve ser seu ponto de coleta!";

        vehicleStateService.changeOwnerMeetUpPlaceTo(ONSITE);

        return sendSingleMessage(event, chatNotification);
    }

    @Override
    public Mono<Void> doBaysideInteractionHandler(final ReactionAddEvent event) {
        if (vehicleStateService.getCompanyVehicle().getOwnerMeetUpPlace().equals(BAYSIDE)) {
            return Mono.empty();
        }

        String chatNotification = """
                <@%d> mudou de ideia e definiu o ponto de encontro sendo Bayside !
                > Demais usuários podem interagir para definirem aonde deve ser seu ponto de coleta!
                """.formatted(event.getUserId().asLong());

        vehicleStateService.changeOwnerMeetUpPlaceTo(BAYSIDE);
        return sendSingleMessage(event, chatNotification);
    }

    @Override
    public Mono<Void> doNoneInteractionHandler(final ReactionAddEvent event) {
        return event.getMessage().flatMap(message -> {
            Mono<Void> notifyUsers = message.getChannel().flatMap(messageChannel ->
                    messageChannel.createMessage("<@%d> concluíu a solicitação e a viagem foi excluída !".formatted(event.getUserId().asLong()))
                            .delayElement(Duration.ofSeconds(10))
                            .flatMap(Message::delete)
            );

            Mono<Void> excludeOwnerRideMessage = Mono.fromRunnable(vehicleStateService::cancelRide).then(message.delete());
            return Mono.when(excludeOwnerRideMessage, notifyUsers);
        });
    }

    private Mono<Void> sendSingleMessage(ReactionAddEvent event, String chatNotification) {
        return event.getMessage()
                .flatMap(Message::getChannel)
                .flatMap(messageChannel ->
                        playerNotificationService.sendSingleMessage(messageChannel, chatNotification, event.getUserId().asLong())
                ).then();
    }
}