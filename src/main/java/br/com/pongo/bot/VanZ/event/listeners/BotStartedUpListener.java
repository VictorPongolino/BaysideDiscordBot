package br.com.pongo.bot.VanZ.event.listeners;

import br.com.pongo.bot.VanZ.event.EventListener;
import br.com.pongo.bot.VanZ.service.ChannelMessageDisposalService;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.time.Duration;

@Log4j2
@RequiredArgsConstructor
@Component
public class BotStartedUpListener implements EventListener<ReadyEvent> {

    private final ChannelMessageDisposalService channelMessageDisposalService;

    @Override
    public Class<ReadyEvent> getEventType() {
        return ReadyEvent.class;
    }

    @Override
    public Mono<Void> execute(final ReadyEvent event) {
        channelMessageDisposalService.disposeNonPinnedMessages(Duration.ofMinutes(15));
        return Mono.empty();
    }
}

