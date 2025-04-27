package br.com.pongo.bot.VanZ.command.commands;

import br.com.pongo.bot.VanZ.command.DiscordCommand;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import org.springframework.beans.factory.annotation.Value;
import reactor.core.publisher.Mono;

import java.time.Duration;

public abstract sealed class AbstractDiscordCommand implements DiscordCommand permits ReleaseRiderCommand, StatusCommand, VanCommand {

    @Value("${bot.van-channel-id}")
    protected String allowedChannel;

    @Override
    public String getAllowedChannel() {
        return allowedChannel;
    }

    protected Mono<Message> sendSingleMessageToUserChannel(final MessageCreateEvent event, final String message) {
        return event.getMessage()
                .getChannel()
                .flatMap(messageChannel -> {
                   return messageChannel.createMessage(message);
                });
    }

    protected Mono<Void> sendSingleMessageToUserChannel(final MessageCreateEvent event, final String message, final Duration duration) {
        return sendSingleMessageToUserChannel(event, message)
                .delayElement(duration)
                .flatMap(Message::delete);
    }
}
