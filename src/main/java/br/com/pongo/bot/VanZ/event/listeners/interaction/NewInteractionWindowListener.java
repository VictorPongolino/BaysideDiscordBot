package br.com.pongo.bot.VanZ.event.listeners.interaction;

import br.com.pongo.bot.VanZ.service.EventPublisherService.CommutingRequestStarted;
import discord4j.core.object.entity.Message;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.LocalDateTime;

@Log4j2
@Component
@RequiredArgsConstructor
public class NewInteractionWindowListener {

    private final ApplicationEventPublisher eventPublisher;
    private Disposable disposable;

    @EventListener(CommutingRequestStarted.class)
    public void onNewInteractionWindowStart(final CommutingRequestStarted event) {
        Duration interactionWindow = Duration.between(LocalDateTime.now(), event.getTimeLimitToPlayersInteractWith());

        this.disposable = Mono.delay(interactionWindow)
                .doOnNext(timeUp -> {
                    eventPublisher.publishEvent(InteractionTimeUpEvent
                            .builder()
                            .message(event.getMessage())
                            .userId(event.getUserId())
                            .build());
                })
                .subscribe();
    }

    @Builder @Getter
    public static final class InteractionTimeUpEvent {
        private final Message message;
        private final long userId;
    }
}

