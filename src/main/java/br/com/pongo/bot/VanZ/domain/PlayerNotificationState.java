package br.com.pongo.bot.VanZ.domain;

import discord4j.core.object.entity.Message;
import lombok.Getter;
import reactor.core.publisher.Mono;

@Getter
public class PlayerNotificationState {
    private Mono<Message> message = Mono.empty();

    public Mono<Void> updateLastMessage(final Message newMessage) {
        this.message = this.message
                .flatMap(Message::delete)
                .onErrorResume(x -> Mono.empty())
                .thenReturn(newMessage);

        return message.then();
    }
}
