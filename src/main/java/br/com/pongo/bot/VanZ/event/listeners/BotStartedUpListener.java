package br.com.pongo.bot.VanZ.event.listeners;

import br.com.pongo.bot.VanZ.event.EventListener;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;

@Log4j2
@Component
public class BotStartedUpListener implements EventListener<ReadyEvent> {

    private final String channelId;
    private final GatewayDiscordClient gatewayDiscordClient;

    public BotStartedUpListener(@Value("${bot.van-channel-id}") final String channelId,
                                @Lazy final GatewayDiscordClient gatewayDiscordClient) {
        this.channelId = channelId;
        this.gatewayDiscordClient = gatewayDiscordClient;
    }

    @Override
    public Class<ReadyEvent> getEventType() {
        return ReadyEvent.class;
    }

    @Override
    public Mono<Void> execute(final ReadyEvent event) {
        gatewayDiscordClient.getChannelById(Snowflake.of(channelId))
                .ofType(MessageChannel.class)
                .flatMapMany(messageChannel ->
                    messageChannel.getMessagesBefore(Snowflake.of(Instant.now().minus(Duration.ofHours(1))))
                            .filter(message -> !message.isPinned())
                            .collectList()
                )
                .flatMap(messages -> {
                    if (messages.isEmpty()) {
                        log.info("No messages to exclude from the channel in {}", channelId);
                        return Mono.empty();
                    }

                    return messages.getFirst().getChannel().flatMap(messageChannel -> {
                        if (messageChannel instanceof TextChannel textChannel) {
                            if (messages.size() > 1) {
                                log.info("Excluding in bulk {} messages in the channel id {} ", messages.size(), channelId);
                                return textChannel.bulkDeleteMessages(Flux.fromIterable(messages)).then();
                            }
                        }

                        return Mono.empty();
                    })
                    .switchIfEmpty(Mono.defer(() -> {
                        log.info("Excluding sequentially the total of {} messages in the channel id {} ", messages.size(), channelId);
                        return Flux.fromIterable(messages)
                                .flatMap(message -> message.delete()
                                        .onErrorResume(e -> {
                                            log.error("Failed to exclude one message sequentially. {}", e.getMessage());
                                            return Mono.empty();
                                        }))
                                .then();
                    }));
                })
                .subscribe();

        return Mono.empty();
    }
}
