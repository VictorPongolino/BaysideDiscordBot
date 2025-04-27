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
                Quando um usuário precisar ir para Bayside e tiver uma van disponível, ele pode ajudar outros colegas de equipe que também precisem se locomover até lá.
                Para garantir uma melhor organização e facilitar o transporte em grupo, o usuário deve informar que está com a van utilizando o comando !van.
                
                Ao usar !van, o bot criará uma mensagem interativa no canal. Nessa mensagem, outros usuários poderão registrar onde estão — informando, por exemplo, se estão na empresa ou em Bayside. 
                Assim, o dono da van saberá onde buscar cada pessoa.
                
                Essa interação ficará aberta por 1 minuto. Durante esse tempo, os interessados devem se posicionar na mensagem.
                Após a contagem regressiva de 1 minuto, a van estará liberada para seguir viagem, e o usuário que iniciou o comando (o dono da van) poderá ir até os colegas para buscá-los.
                
                > Importante: o bot serve apenas como ferramenta de organização.\nEle não é um hack ou qualquer tipo de trapaça.\nO bot não detecta se você está realmente com a van no jogo, se está online ou em qual posição você se encontra. Ele apenas facilita a comunicação entre os jogadores para melhor organização.
                
                Ao retornar com a van, o dono pode encerrar a ação clicando no ícone :no_entry_sign: ou digitando o comando !liberar.
        """;

        return gatewayDiscordClient
                .getChannelById(Snowflake.of(channelConfiguration.getAllowedChannel()))
                .map(Channel::getRestChannel)
                .flatMap(restChannel -> restChannel.createMessage(chatMessage))
                .then();
    }
}
