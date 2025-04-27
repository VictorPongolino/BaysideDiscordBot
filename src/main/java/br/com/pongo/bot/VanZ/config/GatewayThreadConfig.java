package br.com.pongo.bot.VanZ.config;

import discord4j.core.GatewayDiscordClient;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;

@Configuration
@RequiredArgsConstructor
public class GatewayThreadConfig {
    private final GatewayDiscordClient gatewayDiscordClient;

    @EventListener(ApplicationReadyEvent.class)
    public void startBot() {
        gatewayDiscordClient.onDisconnect().block();
    }
}
