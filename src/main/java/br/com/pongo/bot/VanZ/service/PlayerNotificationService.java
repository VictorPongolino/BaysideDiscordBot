package br.com.pongo.bot.VanZ.service;

import br.com.pongo.bot.VanZ.domain.PlayerNotificationState;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class PlayerNotificationService {
    private final Map<Long, PlayerNotificationState> players = new HashMap<>();

    public Mono<Void> addOrUpdateLastPlayerMessage(final Long identifier, final Message message) {
        PlayerNotificationState state = players.computeIfAbsent(identifier, id -> new PlayerNotificationState());
        return state.updateLastMessage(message);
    }


    public Mono<Void> sendSingleMessage(final MessageChannel messageChannel, final String chatNotification, final Long identifier) {
        return messageChannel.createMessage(chatNotification)
                    .flatMap(message -> addOrUpdateLastPlayerMessage(identifier, message))
                    .then();
    }
}
