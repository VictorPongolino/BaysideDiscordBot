package br.com.pongo.bot.VanZ.command.commands;

import br.com.pongo.bot.VanZ.command.DiscordCommand;
import br.com.pongo.bot.VanZ.config.ChannelConfiguration;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import reactor.core.publisher.Mono;

import java.time.Duration;

public abstract sealed class AbstractDiscordCommand implements DiscordCommand permits BaysideBotCommand, ReleaseRiderCommand, StatusCommand, VanCommand {

    private final ChannelConfiguration channelConfiguration;

    protected AbstractDiscordCommand(final ChannelConfiguration channelConfiguration) {
        this.channelConfiguration = channelConfiguration;
    }

    @Override
    public String getAllowedChannel() {
        return channelConfiguration.getAllowedChannel();
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
