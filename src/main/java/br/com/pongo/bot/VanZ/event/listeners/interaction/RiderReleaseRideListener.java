package br.com.pongo.bot.VanZ.event.listeners.interaction;

import br.com.pongo.bot.VanZ.event.RideReleaseConcludedEvent;
import br.com.pongo.bot.VanZ.event.RideReleaseRequestedEvent;
import br.com.pongo.bot.VanZ.event.enums.ConclusionType;
import br.com.pongo.bot.VanZ.service.RideReleaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;


@Component
@Log4j2
@RequiredArgsConstructor
public class RiderReleaseRideListener {

    private final RideReleaseService rideReleaseService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @EventListener(value = RideReleaseRequestedEvent.class,
            condition =  "#event.conclusionType.name() == 'FORCED'")
    public void onInteractionTimeOut(final RideReleaseRequestedEvent event) {
        String chatMessage = "<@%d> forçou a liberação da van alocado antes por <@%d>".formatted(event.getRequestedByUserId(), event.getOwnerId());
        rideReleaseService.finishAndReport(chatMessage)
                .doOnNext(concluded -> {
                    RideReleaseConcludedEvent rideReleaseConcludedEvent = RideReleaseConcludedEvent.builder()
                            .oldOwner(event.getOwnerId())
                            .requestedByUserId(event.getRequestedByUserId())
                            .conclusionType(ConclusionType.FORCED)
                            .build();

                    applicationEventPublisher.publishEvent(rideReleaseConcludedEvent);
                })
                .subscribe();
    }
}

