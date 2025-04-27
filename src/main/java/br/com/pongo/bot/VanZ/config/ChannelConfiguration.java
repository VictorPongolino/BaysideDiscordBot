package br.com.pongo.bot.VanZ.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Getter
@Component
public class ChannelConfiguration {
    @Value("${bot.van-channel-id}")
    private String allowedChannel;

}
