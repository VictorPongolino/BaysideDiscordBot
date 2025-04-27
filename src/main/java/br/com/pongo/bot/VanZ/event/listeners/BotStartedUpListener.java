package br.com.pongo.bot.VanZ.event.listeners;

import br.com.pongo.bot.VanZ.config.ChannelConfiguration;
import br.com.pongo.bot.VanZ.event.EventListener;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Log4j2
@Component
public class BotStartedUpListener implements EventListener<ReadyEvent> {

    private final ChannelConfiguration channelConfiguration;
    private final GatewayDiscordClient gatewayDiscordClient;

    public BotStartedUpListener(final ChannelConfiguration channelConfiguration,
                                @Lazy final GatewayDiscordClient gatewayDiscordClient) {
        this.channelConfiguration = channelConfiguration;
        this.gatewayDiscordClient = gatewayDiscordClient;
    }

    @Override
    public Class<ReadyEvent> getEventType() {
        return ReadyEvent.class;
    }

    @Override
    public Mono<Void> execute(final ReadyEvent event) {
        gatewayDiscordClient.getChannelById(Snowflake.of(channelConfiguration.getAllowedChannel()))
                .ofType(MessageChannel.class)
                .flatMapMany(messageChannel ->
                    messageChannel.getMessagesBefore(Snowflake.of(Instant.now().minus(Duration.ofMinutes(15))))
                            .filter(message -> !message.isPinned())
                            .collectList()
                )
                .flatMap(messages -> {
                    if (messages.isEmpty()) {
                        log.info("No messages to exclude from the channel in {}", channelConfiguration.getAllowedChannel());
                        return Mono.empty();
                    }

                    return messages.getFirst().getChannel().flatMap(messageChannel -> {
                        if (messageChannel instanceof TextChannel textChannel) {
                            if (messages.size() > 1) {
                                log.info("Excluding in bulk {} messages in the channel id {} ", messages.size(), channelConfiguration.getAllowedChannel());
                                return textChannel.bulkDeleteMessages(Flux.fromIterable(messages))
                                        .onErrorResume(e -> {
                                            log.error("Failed to exclude bulk messages. {}", e.getMessage());
                                            deleteMessagesSequentially(messages);
                                            return Mono.empty();
                                        }).then();
                            }
                        }

                        return deleteMessagesSequentially(messages);
                    });
                })
                .subscribe();

        return Mono.empty();
    }

    private Mono<Void> deleteMessagesSequentially(final List<Message> messages) {
        log.info("Excluding sequentially the total of {} messages in the channel id {} ", messages.size(), channelConfiguration.getAllowedChannel());
        return Flux.fromIterable(messages)
                .flatMap(message -> message.delete()
                        .onErrorResume(e -> {
                            log.error("Failed to exclude one message sequentially. {}", e.getMessage());
                            return Mono.empty();
                        }))
                .then();
    }
}
