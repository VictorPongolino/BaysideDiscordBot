package br.com.pongo.bot.VanZ.service;

import discord4j.core.object.entity.Message;
import lombok.Builder;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class EventPublisherService {

    private final Duration maximumTimeToInteraction;
    private final ApplicationEventPublisher eventPublisher;

    public EventPublisherService(
            @Value("${interaction.maxTimeToInteract}") final Duration maximumTimeToInteraction,
            final ApplicationEventPublisher eventPublisher) {
        this.maximumTimeToInteraction = maximumTimeToInteraction;
        this.eventPublisher = eventPublisher;
    }

    public void broadcastInteractionWindowStart(final long identifier, final Message message) {
        CommutingRequestStarted event = CommutingRequestStarted.builder()
            .timeLimitToPlayersInteractWith(LocalDateTime.now().plus(maximumTimeToInteraction))
            .message(message)
            .userId(identifier)
            .build();

        eventPublisher.publishEvent(event);
    }

    @Builder @Getter
    public static class CommutingRequestStarted {
        private final LocalDateTime timeLimitToPlayersInteractWith;
        private final Message message;
        private final long userId;
    }
}