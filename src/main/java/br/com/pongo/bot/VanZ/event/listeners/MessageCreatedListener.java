package br.com.pongo.bot.VanZ.event.listeners;


import br.com.pongo.bot.VanZ.command.DiscordCommand;
import br.com.pongo.bot.VanZ.event.EventListener;
import discord4j.core.event.domain.message.MessageCreateEvent;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
public class MessageCreatedListener implements EventListener<MessageCreateEvent> {

    private final Map<String, DiscordCommand> registeredCommands;

    public MessageCreatedListener(final List<DiscordCommand> discordCommands) {
        registeredCommands = discordCommands.stream()
                .collect(Collectors.toMap(DiscordCommand::getName, Function.identity()));
    }

    @Override
    public Class<MessageCreateEvent> getEventType() {
        return MessageCreateEvent.class;
    }

    @Override
    public Mono<MessageCreateEvent> filter(MessageCreateEvent event) {
        return Mono.just(event.getMessage())
                .filter(
                        message -> message.getAuthor().map(user -> !user.isBot()).orElse(false)
                )
                .map(x -> event);
    }

    @Override
    public Mono<Void> execute(final MessageCreateEvent event) {
        return Mono.just(event.getMessage())
                .flatMap(message -> {
                    String messageContent = message.getContent().toLowerCase().trim();
                    DiscordCommand command = registeredCommands.get(messageContent);

                    if (command != null) {
                        return command.filter(event)
                                .flatMap(command::handle)
                                .then(message.delete());
                    }

                    return Mono.empty();
                })
                .then();
    }
}
