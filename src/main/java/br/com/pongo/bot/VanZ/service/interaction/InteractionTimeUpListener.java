package br.com.pongo.bot.VanZ.service.interaction;

import br.com.pongo.bot.VanZ.domain.CompanyVehicle.OwnerMeetUpPlace;
import br.com.pongo.bot.VanZ.domain.CompanyVehicle.Passenger;
import br.com.pongo.bot.VanZ.service.DiscordNotificationService;
import br.com.pongo.bot.VanZ.service.VehicleStateService;
import br.com.pongo.bot.VanZ.service.interaction.NewInteractionWindowListener.InteractionTimeUpEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
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
        String passengerInBayside = getPassengersByMeetUp(Passenger.MeetUpPreference.BAYSIDE);

        if (isOwnerMeetingUpAt(OwnerMeetUpPlace.ONSITE) || isOwnerMeetingUpAt(OwnerMeetUpPlace.DEFAULT)) {
            chatMessage.append("Bayside");
            chatMessage.append("\n");

            if (!passengerInBayside.isBlank()) {
                chatMessage.append("\nLá você deverá aguardar pelos seguintes jogadores:\n");
                chatMessage.append(delimiter);
                chatMessage.append(passengerInBayside);
            }
        } else {
            chatMessage.append("a Empresa.");

            String passengerOnSite = getPassengersByMeetUp(Passenger.MeetUpPreference.ONSITE);

            if (!passengerOnSite.isEmpty()) {
                chatMessage.append(" Porém, aguarde pelos jogadores:\n");
                chatMessage.append(delimiter);
                chatMessage.append(passengerOnSite);
            }

            if (!passengerInBayside.isEmpty()) {
                chatMessage.append("\n\nAo chegar em Bayside, aguarde pelos jogadores:\n");
                chatMessage.append(delimiter);
                chatMessage.append(passengerInBayside);
            }
        }

        chatMessage.append("\n\nLembrando que após a conclusão da viagem, liberar o comando!");

        event.getMessage().getChannel()
                .flatMap(msgChannel -> msgChannel.createMessage(chatMessage.toString()))
                .subscribe();
    }

    private boolean isOwnerMeetingUpAt(OwnerMeetUpPlace ownerMeetUpPlace) {
        return vehicleStateService.getCompanyVehicle().getOwnerMeetUpPlace().equals(ownerMeetUpPlace);
    }

    private String getPassengersByMeetUp(Passenger.MeetUpPreference meetUpPreference) {
        return vehicleStateService
                .getCompanyVehicle()
                .getPassengersByMeetUp(meetUpPreference)
                .stream()
                .map(Passenger::getUserIdentifier)
                .map(String::valueOf)
                .collect(Collectors.joining("\n - "));
    }
}
