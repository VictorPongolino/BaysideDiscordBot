package br.com.pongo.bot.VanZ.event.listeners.interaction;

import br.com.pongo.bot.VanZ.domain.CompanyVehicle.OwnerMeetUpPlace;
import br.com.pongo.bot.VanZ.domain.CompanyVehicle.Passenger.MeetUpPreference;
import br.com.pongo.bot.VanZ.service.DiscordNotificationService;
import br.com.pongo.bot.VanZ.service.VehicleStateService;
import br.com.pongo.bot.VanZ.event.listeners.interaction.NewInteractionWindowListener.InteractionTimeUpEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
@Log4j2
@RequiredArgsConstructor
public class InteractionTimeUpListener {

    private final DiscordNotificationService notificationService;
    private final VehicleStateService vehicleStateService;

    @EventListener(InteractionTimeUpEvent.class)
    public void onInteractionTimeOut(final InteractionTimeUpEvent event) {
        vehicleStateService.allowOwnerToStartJourney();

        StringBuilder chatMessage = new StringBuilder();
        chatMessage.append("<@%d> está liberado para ir até ".formatted(event.getUserId()));

        final String delimiter = "\n - ";
        String passengerInBayside = getPassengersByMeetUp(MeetUpPreference.BAYSIDE, delimiter);
        log.info("Passengers in Bayside: '{}' (Length: {})", passengerInBayside, passengerInBayside.length());

        if (isOwnerMeetingUpAt(OwnerMeetUpPlace.ONSITE) || isOwnerMeetingUpAt(OwnerMeetUpPlace.DEFAULT)) {
            chatMessage.append("Bayside");
            chatMessage.append("\n");

            if (!passengerInBayside.isBlank()) {
                chatMessage.append("\nLá você deverá aguardar pelos seguintes jogadores:");
                chatMessage.append(delimiter);
                chatMessage.append(passengerInBayside);
            }
        } else {
            chatMessage.append("a Empresa.");

            String passengerOnSite = getPassengersByMeetUp(MeetUpPreference.ONSITE, delimiter);
            log.info("Passengers onsite: '{}' (Length: {})", passengerOnSite, passengerOnSite.length());

            if (!passengerOnSite.isEmpty()) {
                chatMessage.append(" Porém, aguarde pelos jogadores:");
                chatMessage.append(delimiter);
                chatMessage.append(passengerOnSite);
            }

            if (!passengerInBayside.isEmpty()) {
                chatMessage.append("\n\nAo chegar em Bayside, aguarde pelos jogadores:");
                chatMessage.append(delimiter);
                chatMessage.append(passengerInBayside);
            }
        }

        chatMessage.append("\n\nLembrando que após a conclusão da viagem, liberar o comando!");

        event.getMessage().getChannel()
                .flatMap(msgChannel -> msgChannel.createMessage(chatMessage.toString()))
                .subscribe();
    }

    private boolean isOwnerMeetingUpAt(final OwnerMeetUpPlace ownerMeetUpPlace) {
        return vehicleStateService.getCompanyVehicle().getOwnerMeetUpPlace().equals(ownerMeetUpPlace);
    }

    private String getPassengersByMeetUp(final MeetUpPreference meetUpPreference, final String delimiter) {
        return vehicleStateService
                .getCompanyVehicle()
                .getPassengersByMeetUp(meetUpPreference)
                .stream()
                .map(passenger -> "<@%d>".formatted(passenger.getUserIdentifier()))
                .collect(Collectors.joining(delimiter));
    }
}
