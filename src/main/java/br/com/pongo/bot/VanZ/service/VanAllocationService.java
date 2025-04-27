package br.com.pongo.bot.VanZ.service;

import discord4j.core.object.entity.channel.MessageChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class VanAllocationService {
    private final VehicleStateService vehicleStateService;
    private final DiscordNotificationService discordNotificationService;
    private final EventPublisherService eventPublisherService;

    public void allocateVehicleTo(final Long identifier, final Mono<MessageChannel> channel) {
        vehicleStateService.assignOwnerAndReset(identifier);

        discordNotificationService.notifyUsersAboutInteraction(identifier, channel)
                .doOnNext(message -> eventPublisherService.broadcastInteractionWindowStart(identifier, message))
                .subscribe();
    }

    public boolean hasOwner() {
        return vehicleStateService.getCompanyVehicle().hasOwner();
    }
}

