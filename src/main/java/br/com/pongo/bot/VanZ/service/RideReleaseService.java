package br.com.pongo.bot.VanZ.service;

import br.com.pongo.bot.VanZ.config.ChannelConfiguration;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.channel.Channel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Log4j2
@RequiredArgsConstructor
@Service
public class RideReleaseService {

    private final VehicleStateService vehicleStateService;
    private final GatewayDiscordClient gatewayDiscordClient;
    private final ChannelConfiguration channelConfiguration;

    public Mono<Void> finishAndReport(final String chatMessage) {
        return gatewayDiscordClient.getChannelById(Snowflake.of(channelConfiguration.getAllowedChannel()))
                .map(Channel::getRestChannel)
                .filter(x -> vehicleStateService.getCompanyVehicle().hasOwner())
                .flatMap(restChannel ->
                        restChannel.createMessage(chatMessage)
                ).then(Mono.defer(() -> {
                    vehicleStateService.cancelRide();
                    return Mono.empty();
                }));
    }
}
