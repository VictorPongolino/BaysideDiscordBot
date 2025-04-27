package br.com.pongo.bot.VanZ.event.listeners;

import br.com.pongo.bot.VanZ.service.ChannelMessageDisposalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@Log4j2
@RequiredArgsConstructor
public class RideReleaseConcludedListener {
    private final ChannelMessageDisposalService channelMessageDisposalService;

    @EventListener(RideReleaseConcludedListener.class)
    public void onEvent(final RideReleaseConcludedListener rideReleaseConcludedListener) {
        final Duration minMessageTimeToExclude = Duration.ofMinutes(5);
        log.info("Excluding previous messages with duration as {}", minMessageTimeToExclude);
        channelMessageDisposalService.disposeNonPinnedMessages(minMessageTimeToExclude);
    }
}
