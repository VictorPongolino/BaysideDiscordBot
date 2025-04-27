package br.com.pongo.bot.VanZ.command.commands;

import br.com.pongo.bot.VanZ.config.ChannelConfiguration;
import br.com.pongo.bot.VanZ.domain.CompanyVehicle;
import br.com.pongo.bot.VanZ.service.VehicleStateService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
public final class ReleaseRiderCommand extends AbstractDiscordCommand {

    private final VehicleStateService vanAllocationService;

    private ReleaseRiderCommand(final ChannelConfiguration channelConfiguration, VehicleStateService vanAllocationService) {
        super(channelConfiguration);
        this.vanAllocationService = vanAllocationService;
    }

    @Override
    public String getName() {
        return "!liberar";
    }

    @Override
    public Mono<Void> handle(final MessageCreateEvent event) {
        CompanyVehicle companyVehicle = vanAllocationService.getCompanyVehicle();

        if (!companyVehicle.hasOwner()) {
            sendSingleMessageToUserChannel(event, "<@%d> não há ninguém usando a van!".formatted(event.getMessage().getUserData().id().asLong()))
                    .subscribe();
            return Mono.empty();
        }

        sendSingleMessageToUserChannel(event, "<@%d> forçou a liberação da van alocado antes por <@%d>".formatted(event.getMessage().getUserData().id().asLong(), companyVehicle.getOwnerId()))
                .then(Mono.defer(() -> {
                    companyVehicle.resetRide();
                    return Mono.empty();
                }))
                .subscribe();

        return Mono.empty();
    }
}
