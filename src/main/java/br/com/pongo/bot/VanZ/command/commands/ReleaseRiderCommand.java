package br.com.pongo.bot.VanZ.command.commands;

import br.com.pongo.bot.VanZ.config.ChannelConfiguration;
import br.com.pongo.bot.VanZ.service.DiscordNotificationService;
import br.com.pongo.bot.VanZ.service.RideReleaseService;
import br.com.pongo.bot.VanZ.service.VehicleStateService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
public final class ReleaseRiderCommand extends AbstractDiscordCommand {

    private final VehicleStateService vehicleStateService;
    private final RideReleaseService rideReleaseService;
    private final DiscordNotificationService discordNotificationService;

    private ReleaseRiderCommand(final ChannelConfiguration channelConfiguration,
                                final VehicleStateService vehicleStateService,
                                final RideReleaseService rideReleaseService,
                                final DiscordNotificationService discordNotificationService) {
        super(channelConfiguration);
        this.vehicleStateService = vehicleStateService;
        this.rideReleaseService = rideReleaseService;
        this.discordNotificationService = discordNotificationService;
    }

    @Override
    public String getName() {
        return "!liberar";
    }

    @Override
    public Mono<Void> handle(final MessageCreateEvent event) {

        if (!vehicleStateService.getCompanyVehicle().hasOwner()) {
            return discordNotificationService.createMessageForAllowedChannel("Hey! <@%d>, não há ninguém usando van para ser liberada!".formatted(event.getMessage().getUserData().id().asLong())).then();
        }

        long requestId = event.getMessage().getUserData().id().asLong();
        Long ownerId = vehicleStateService.getCompanyVehicle().getOwnerId();

        StringBuilder stringBuilder = new StringBuilder("<@%d>".formatted(requestId));
        if (ownerId == requestId) {
            stringBuilder.append("não esta mais utilizando a van.");
        } else {
            stringBuilder.append("liberou a van antes sendo utilizada por <@%d>".formatted(ownerId));
        }

        return rideReleaseService.finishAndReport(stringBuilder.toString());
    }
}
