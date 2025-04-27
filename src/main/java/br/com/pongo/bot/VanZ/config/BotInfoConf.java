package br.com.pongo.bot.VanZ.config;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.User;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BotInfoConf {

    private final GatewayDiscordClient gatewayDiscordClient;
    private final BotInfo botInfo;

    @PostConstruct
    public void configure() {
        gatewayDiscordClient.getSelf()
                .map(User::getId)
                .map(Snowflake::asLong)
                .doOnNext(botInfo::setId)
                .subscribe();
    }

    @Getter
    @Component
    @NoArgsConstructor
    public static final class BotInfo {
        private long id;

        void setId(final long id) {
            this.id = id;
        }
    }
}
