package br.com.pongo.bot.VanZ.command.commands;

import br.com.pongo.bot.VanZ.config.ChannelConfiguration;
import br.com.pongo.bot.VanZ.domain.VehicleInteractionConfig;
import br.com.pongo.bot.VanZ.service.VehicleStateService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Component
public final class StatusCommand extends AbstractDiscordCommand {

    private final VehicleStateService vehicleStateService;
    private final VehicleInteractionConfig vehicleInteractionConfig;

    private StatusCommand(final ChannelConfiguration channelConfiguration,
                          final VehicleStateService vehicleStateService,
                          final VehicleInteractionConfig vehicleInteractionConfig) {
        super(channelConfiguration);
        this.vehicleStateService = vehicleStateService;
        this.vehicleInteractionConfig = vehicleInteractionConfig;
    }

    @Override
    public String getName() {
        return "!status";
    }

    @Override
    public Mono<Void> handle(final MessageCreateEvent event) {
        String chatMessage;
        if (vehicleStateService.getCompanyVehicle().hasOwner()) {
            chatMessage = "<@%d> esta com a van!\n> Caso mesmo não esteja, ele poderá interagir com %s !".formatted(vehicleStateService.getCompanyVehicle().getOwnerId(), vehicleInteractionConfig.getNoneMeeting().getUnicodeRaw());
        }
        else {
            chatMessage = "Hey <@%d>! Não há ninguém com a van neste momento!\n> use: _!van_ para utilizar a van!".formatted(event.getMessage().getUserData().id().asLong());
        }

        return sendSingleMessageToUserChannel(event, chatMessage, Duration.ofSeconds(30));
    }
}
