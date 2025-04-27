package br.com.pongo.bot.VanZ.event.reaction;

import br.com.pongo.bot.VanZ.domain.VehicleInteractionConfig;
import discord4j.core.event.domain.message.ReactionAddEvent;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

public interface ReactionInteractionHandler {
    Map<String, VehicleInteractionConfig.MeetingDetails> reactionCommand = new HashMap<>();

    Mono<Void> doOnsiteInteractionHandler(ReactionAddEvent event);
    Mono<Void> doBaysideInteractionHandler(ReactionAddEvent event);
    Mono<Void> doNoneInteractionHandler(ReactionAddEvent event);
}
