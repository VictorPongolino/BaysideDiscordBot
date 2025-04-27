package br.com.pongo.bot.VanZ.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public interface DiscordCommand {
    Logger LOG = LoggerFactory.getLogger(DiscordCommand.class);

    String ALL_CHANNELS_ALLOWED = "";

    String getName();
    Mono<Void> handle(MessageCreateEvent event);

    default String getAllowedChannel() {
        return ALL_CHANNELS_ALLOWED;
    }

    default Mono<MessageCreateEvent> filter(final MessageCreateEvent event) {
        final Message message = event.getMessage();

        return Mono.just(message)
                .filter(this::filterAnyBotMessage)
                .filterWhen(this::filterMessagesToChannel)
                .switchIfEmpty(Mono.defer(() ->
                    message.getChannel()
                            .flatMap(messageChannel -> {
                                String adviceMessage;
                                if (message.getAuthor().isPresent()) {
                                    long userId = message.getAuthor().get().getId().asLong();
                                    adviceMessage = "Hey <@%d>, este comando apenas pode ser executado em <#%d> !".formatted(userId, Long.parseLong(getAllowedChannel()));
                                } else {
                                    adviceMessage = "Olá, comandos de transporte apenas podem ser executados em <#%d> !".formatted( Long.parseLong(getAllowedChannel()));
                                }

                                return messageChannel.createMessage(adviceMessage);
                           })
                            .then(Mono.empty())
                ))
                .map(x -> event);
    }

    private Mono<Boolean> filterMessagesToChannel(final Message message) {
        if (ALL_CHANNELS_ALLOWED.equals(getAllowedChannel())) {
            return Mono.just(true);
        }

        return message.getChannel()
                .map(messageChannel -> messageChannel.getId().asString().equals(getAllowedChannel()))
                .defaultIfEmpty(false);
    }

    private Boolean filterAnyBotMessage(final Message message) {
        return message.getAuthor().map(user -> !user.isBot()).orElse(false);
    }
}
