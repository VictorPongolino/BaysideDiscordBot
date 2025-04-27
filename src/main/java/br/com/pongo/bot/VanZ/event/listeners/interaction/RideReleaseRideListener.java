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
public class RideReleaseRideListener {

    private final RideReleaseService rideReleaseService;
    private final ApplicationEventPublisher applicationEventPublisher;

    @EventListener(value = RideReleaseRequestedEvent.class,
            condition =  "#event.conclusionType.name() == 'NORMAL'")
    public void onInteractionTimeOut(final RideReleaseRequestedEvent event) {
        log.info("Received the event for releasing the rider normally.");
        String chatMessage = "<@%d> concluíu a solicitação e a viagem foi excluída !".formatted(event.getOwnerId());
        rideReleaseService.finishAndReport(chatMessage)
                .doOnNext(concluded -> {
                    RideReleaseConcludedEvent rideReleaseConcludedEvent = RideReleaseConcludedEvent.builder()
                            .oldOwner(event.getOwnerId())
                            .requestedByUserId(event.getRequestedByUserId())
                            .conclusionType(ConclusionType.NORMAL)
                            .build();

                    applicationEventPublisher.publishEvent(rideReleaseConcludedEvent);
                })
                .subscribe();
    }
}
