package br.com.pongo.bot.VanZ.config;

import br.com.pongo.bot.VanZ.event.EventListener;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@Log4j2
@RequiredArgsConstructor
public class EventListenerConfig {
    private final GatewayDiscordClient client;
    private final List<EventListener<?>> eventListeners;

    @PostConstruct
    void init() {
        eventListeners.forEach(listener -> registerEvent(client, listener));
    }

    public <T extends Event> void registerEventsFromApplicationContext(final GatewayDiscordClient client, final List<EventListener<T>> eventListeners) {
        log.info("Detected total of {} events", eventListeners.size());
        eventListeners.forEach(listener -> registerEvent(client, listener));
    }

    private <T extends Event> void registerEvent(GatewayDiscordClient client, EventListener<T> listener) {
        log.info("Added event listener of type {}", listener.getEventType());
        client.on(listener.getEventType())
                .flatMap(listener::filter)
                .flatMap(listener::execute)
                .onErrorResume(listener::handleError)
                .subscribe();
    }
}

