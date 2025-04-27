package br.com.pongo.bot.VanZ.service;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class UserService {

    private final GatewayDiscordClient gatewayDiscordClient;

    public Mono<User> getUserByIdentificationId(final Long identification) {
        Snowflake ownerFlake = Snowflake.of(identification);
        return gatewayDiscordClient.getUserById(ownerFlake);
    }
}
