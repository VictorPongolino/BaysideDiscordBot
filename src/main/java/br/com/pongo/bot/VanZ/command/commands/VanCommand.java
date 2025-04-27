package br.com.pongo.bot.VanZ.command.commands;

import br.com.pongo.bot.VanZ.domain.CompanyVehicle;
import br.com.pongo.bot.VanZ.service.VanAllocationService;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Optional;


@Component
@RequiredArgsConstructor
public final class VanCommand extends AbstractDiscordCommand {

    private final CompanyVehicle companyVehicle;
    private final VanAllocationService vanAllocationService;

    @Override
    public String getName() {
        return "!van";
    }

    @Override
    public Mono<Void> handle(final MessageCreateEvent event) {
        Optional<Long> authorIdentity = event.getMessage().getAuthor()
                .map(user -> user.getId().asLong());

        if (authorIdentity.isEmpty()) {
            return Mono.error(new IllegalArgumentException("Not found user-identify to process %s command!".formatted(getName())));
        }

        Mono<MessageChannel> channel = event.getMessage().getChannel();

        if (vanAllocationService.hasOwner()) {
            sendSingleChannelMessage(channel, "Ops! <@%d> já está com a \uD83D\uDE9A !!".formatted(companyVehicle.getOwnerId()));
            return Mono.empty();
        }

        vanAllocationService.allocateVehicleTo(authorIdentity.get(), channel);
        return Mono.empty();
    }

    private void sendSingleChannelMessage(final Mono<MessageChannel> messageChannel, final String message) {
        messageChannel.flatMap(channel -> channel.createMessage(message)).subscribe();
    }
}
