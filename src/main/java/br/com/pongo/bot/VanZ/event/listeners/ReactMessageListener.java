package br.com.pongo.bot.VanZ.event.listeners;

import br.com.pongo.bot.VanZ.config.BotInfoConf.BotInfo;
import br.com.pongo.bot.VanZ.domain.VehicleInteractionConfig;
import br.com.pongo.bot.VanZ.event.EventListener;
import br.com.pongo.bot.VanZ.event.reaction.ReactionInteractionDelegator;
import br.com.pongo.bot.VanZ.service.VehicleStateService;
import discord4j.core.event.domain.message.ReactionAddEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.reaction.Reaction;
import discord4j.core.object.reaction.ReactionEmoji;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.stream.Stream;

@Component
@Log4j2
@RequiredArgsConstructor
public class ReactMessageListener implements EventListener<ReactionAddEvent> {

    private final BotInfo botInfo;
    private final VehicleStateService vehicleStateService;
    private final VehicleInteractionConfig vehicleInteractionConfig;
    private final ReactionInteractionDelegator reactionInteractionDelegator;

    @Value("${bot.van-channel-id}")
    private String allowedChannel;

    @Override
    public Class<ReactionAddEvent> getEventType() {
        return ReactionAddEvent.class;
    }

    @Override
    public Mono<ReactionAddEvent> filter(final ReactionAddEvent event) {
        return event.getUser()
                .filter(user -> !user.isBot())
                .filter(user -> vehicleStateService.getCompanyVehicle().hasOwner())
                .flatMap(user -> event.getMessage()
                        .filter(this::isMessageFromTheBot)
                        .filterWhen(this::isMessageBelongToVehicleChannel)
                        .filterWhen(message -> filterUserReacted(event, user, message))
                )
                .filter(message -> isReactionValidEmojiInteraction(event))
                .filterWhen(message -> {
                    if (vehicleStateService.isAlreadyDeparted()) {
                        var isOwner = vehicleStateService.getCompanyVehicle().getOwnerId() == event.getUserId().asLong();
                        if (!isNoneReaction(event) || !isOwner) {
                            return undoNotAllowedParticipantUserReactionForTimeUpInteractions(event, message).thenReturn(false);
                        }
                    }
                    return Mono.just(true);
                })
                .flatMap(message -> undoPreviousUserReactions(event))
                .map(x -> event);
    }

    @Override
    public Mono<Void> execute(final ReactionAddEvent event) {
        return event.getUser().flatMap(user -> {
            final long userIdentifier = user.getId().asLong();
            return reactionInteractionDelegator.delegate(userIdentifier, event).then(Mono.empty());
        });
    }

    private Mono<ReactionAddEvent> undoPreviousUserReactions(final ReactionAddEvent event) {
        return event.getMessage()
                .map(message -> {
                    Stream<ReactionEmoji> getAllReactionsExceptCurrent = message.getReactions()
                            .stream()
                            .map(Reaction::getEmoji)
                            .filter(reactionEmoji -> !reactionEmoji.equals(event.getEmoji()));

                    return Flux.fromStream(getAllReactionsExceptCurrent)
                            .flatMap(reactionEmoji ->
                                    message.getReactors(reactionEmoji)
                                            .filter(allUsersReactedEmoji ->
                                                    allUsersReactedEmoji.getId().asLong() == event.getUserId().asLong()
                                            )
                                            .flatMap(ownerEventReacted ->
                                                message.removeReaction(reactionEmoji, event.getUserId())
                                            )
                            )
                            .subscribe();
                })
                .map(e -> event);
    }

    private Mono<Void> undoNotAllowedParticipantUserReactionForTimeUpInteractions(final ReactionAddEvent event, final Message message) {
        return message.removeReaction(event.getEmoji(), event.getUserId())
                .then(message.getChannel())
                .flatMap(channel -> {
                            String chatMessage = """
                                        <@%d> tempo exedido.\n> Novas interações não são permitidas.
                                        """.formatted(event.getUserId().asLong());
                            return channel.createMessage(chatMessage)
                                    .delayElement(Duration.ofSeconds(30))
                                    .flatMap(Message::delete);
                        }
                );
    }

    private Mono<Boolean> isMessageBelongToVehicleChannel(final Message message) {
        return message.getChannel()
                .map(channel -> allowedChannel.equals(channel.getId().asString()));

    }

    private Boolean isMessageFromTheBot(final Message message) {
        return message.getAuthor()
                .map(user -> user.getId().asLong() == botInfo.getId())
                .orElse(false);
    }

    private Mono<Boolean> filterUserReacted(ReactionAddEvent event, User user, Message message) {
        return message.getReactors(event.getEmoji())
                .filter(reactUser -> reactUser.getId().asLong() == user.getId().asLong())
                .next().hasElement();
    }

    private Boolean isReactionValidEmojiInteraction(final ReactionAddEvent event) {
        return event.getEmoji()
                    .asUnicodeEmoji()
                    .map(unicode ->
                            vehicleInteractionConfig.getMeetingDetailsForEmoji(unicode.getRaw()).isPresent()
                    ).orElse(false);
    };

    private boolean isNoneReaction(final ReactionAddEvent event) {
        var optional = vehicleInteractionConfig.getMeetingDetailsForEmoji(event.getEmoji().asUnicodeEmoji().get().getRaw());
        return optional.isPresent() && optional.get().equals(vehicleInteractionConfig.getNoneMeeting());
    }
}