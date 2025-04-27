package br.com.pongo.bot.VanZ.service;

import br.com.pongo.bot.VanZ.config.ChannelConfiguration;
import br.com.pongo.bot.VanZ.domain.VehicleInteractionConfig;
import br.com.pongo.bot.VanZ.domain.VehicleInteractionConfig.MeetingDetails;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import discord4j.discordjson.json.MessageData;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static br.com.pongo.bot.VanZ.domain.VehicleInteractionConfig.MeetUpInteractions.*;

@Service
@RequiredArgsConstructor
public class DiscordNotificationService {
    private final VehicleInteractionConfig vehicleInteractionConfig;
    private final GatewayDiscordClient gatewayDiscordClient;
    private final ChannelConfiguration channelConfiguration;

    private MeetingDetails onSite;
    private MeetingDetails bayside;
    private MeetingDetails none;

    @PostConstruct
    public void init() {
        this.onSite = vehicleInteractionConfig.getMeetingInteractionFor(ON_SITE);
        this.bayside = vehicleInteractionConfig.getMeetingInteractionFor(BAYSIDE);
        this.none = vehicleInteractionConfig.getMeetingInteractionFor(NONE);
    }

    public Mono<Message> notifyUsersAboutInteraction(final Long userId, final Mono<MessageChannel> channel) {
        String chatMessage = """
                @here <@%d> solicitou o uso do veículo empresa.\n
                > :stopwatch: Usuários interessados tem até 1 minuto para interagir!\n
                *Qual o seu local de encontro?*\n
                _Menu_\n
                - Selecione %s para Empresa
                - Selecione %s para Bayside
                - Selecione %s sem interesse (Opcional/Padrão)\n
                Selecione uma das opções abaixo :arrow_down:
                """.formatted(userId, onSite.getUnicodeRaw(), bayside.getUnicodeRaw(), none.getUnicodeRaw());

        return channel.flatMap(msgChannel ->
                msgChannel.createMessage(chatMessage)
                        .flatMap(message ->
                                Flux.concat(
                                        message.addReaction(ReactionEmoji.unicode(onSite.getUnicodeEmoji())),
                                        message.addReaction(ReactionEmoji.unicode(bayside.getUnicodeEmoji())),
                                        message.addReaction(ReactionEmoji.unicode(none.getUnicodeEmoji()))
                                ).then(Mono.just(message))
                        )
        );
    }

    public Mono<MessageData> createMessageForChannel(final Snowflake channel, final String chatMessage) {
        return gatewayDiscordClient.getChannelById(channel)
                .map(Channel::getRestChannel)
                .flatMap(restChannel ->
                        restChannel.createMessage(chatMessage)
                );
    }

    public Mono<MessageData> createMessageForAllowedChannel(final String chatMessage) {
        return createMessageForChannel(channelConfiguration.getAllowedChannelAsSnowFlake(), chatMessage);
    }
}
