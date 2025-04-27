package br.com.pongo.bot.VanZ.service.interaction;

import br.com.pongo.bot.VanZ.service.DiscordNotificationService;
import br.com.pongo.bot.VanZ.service.VehicleStateService;
import br.com.pongo.bot.VanZ.service.interaction.NewInteractionWindowListener.InteractionTimeUpEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InteractionTimeUpListener {

    private final DiscordNotificationService notificationService;
    private final VehicleStateService vehicleStateService;

    @EventListener(InteractionTimeUpEvent.class)
    public void onInteractionTimeOut(final InteractionTimeUpEvent event) {
//        event.getMessage().delete("Tempo limite de reação atingido!");
        vehicleStateService.allowOwnerToStartJourney();
        notificationService.releaseVehicleToCommute(event.getUserId(), event.getMessage().getChannel());
    }
}
