package br.com.pongo.bot.VanZ.config;

import discord4j.core.DiscordClientBuilder;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.Base64;


@Log4j2
@Component
public class Gateway {

    private final String BOT_SECRET_TOKEN;

    public Gateway(@Value("${bot.key}") String botSecretToken) {
        this.BOT_SECRET_TOKEN = new String(Base64.getDecoder().decode(botSecretToken));
    }

    @Bean
    <T extends Event> GatewayDiscordClient gatewayDiscordClient() {
        return DiscordClientBuilder.create(BOT_SECRET_TOKEN)
                .build()
                .login()
                .block();
    }
}

