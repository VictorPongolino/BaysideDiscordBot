package br.com.pongo.bot.VanZ.service;

import br.com.pongo.bot.VanZ.config.ChannelConfiguration;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.entity.channel.TextChannel;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class ChannelMessageDisposalService {

    private final GatewayDiscordClient gatewayDiscordClient;
    private final ChannelConfiguration channelConfiguration;

    public void disposeNonPinnedMessages(final Duration duration) {
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
