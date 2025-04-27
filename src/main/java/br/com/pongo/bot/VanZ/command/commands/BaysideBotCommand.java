package br.com.pongo.bot.VanZ.command.commands;

import br.com.pongo.bot.VanZ.config.ChannelConfiguration;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.Channel;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


@Component
public final class BaysideBotCommand extends AbstractDiscordCommand {

    private final GatewayDiscordClient gatewayDiscordClient;
    private final ChannelConfiguration channelConfiguration;

    private BaysideBotCommand(final ChannelConfiguration channelConfiguration,
                             final GatewayDiscordClient gatewayDiscordClient) {
        super(channelConfiguration);
        this.gatewayDiscordClient = gatewayDiscordClient;
        this.channelConfiguration = channelConfiguration;
    }

    @Override
    public String getName() {
        return "!help";
    }

    @Override
    public Mono<Void> handle(final MessageCreateEvent event) {
        final String chatMessage = """
        ✨ Para garantir _uma melhor organização e facilitar o transporte em grupo_, o usuário **deve** informar que está com a 🚐 utilizando o comando **!van**.
        
        Ao usar !van, o bot criará uma mensagem interativa no <#%s>. Nessa mensagem, outros usuários poderão registrar onde estão — informando, por exemplo, se estão na empresa ou em Bayside. Essa interação é feita via emojis atreladas na mensagem.
        
        Essa interação ficará aberta por 1 minuto. Durante esse tempo, os interessados devem se posicionar na mensagem.
        
        Após a contagem regressiva de 1 minuto, a 🚐 estará liberada para seguir viagem, e o usuário que iniciou o comando (o dono da van) poderá ir até os colegas para buscá-los.
        
        > ✨ Importante: 
        > 🔹 O bot serve **apenas** como ferramenta de organização.
        > 🔹 Ele *não* é um hack ou qualquer tipo de trapaça.
        > 🔹 O bot não detecta se você está realmente com a van no jogo, se está online ou em qual posição você se encontra. 
        > 🔹 Ele apenas facilita a comunicação entre os jogadores para melhor organização.
        
        **Ao retornar com a van**, o dono *deve* encerrar a ação clicando no ícone :no_entry_sign: ou digitando o comando !liberar.
        """.formatted(channelConfiguration.getAllowedChannel());

        return gatewayDiscordClient
                .getChannelById(Snowflake.of(channelConfiguration.getAllowedChannel()))
                .map(Channel::getRestChannel)
                .flatMap(restChannel -> restChannel.createMessage(chatMessage))
                .then();
    }
}
