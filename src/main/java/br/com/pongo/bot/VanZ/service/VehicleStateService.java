package br.com.pongo.bot.VanZ.service;

import br.com.pongo.bot.VanZ.domain.CompanyVehicle;
import br.com.pongo.bot.VanZ.domain.CompanyVehicle.MeetUpStatus;
import br.com.pongo.bot.VanZ.domain.CompanyVehicle.OwnerMeetUpPlace;
import br.com.pongo.bot.VanZ.domain.CompanyVehicle.Passenger;
import br.com.pongo.bot.VanZ.domain.CompanyVehicle.Passenger.MeetUpPreference;
import br.com.pongo.bot.VanZ.exception.InvalidVehicleStateException;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static br.com.pongo.bot.VanZ.domain.CompanyVehicle.Passenger.MeetUpPreference.*;

@Slf4j
@Service
@Getter
@AllArgsConstructor
public class VehicleStateService {
    private final CompanyVehicle companyVehicle;

    public void assignOwnerAndReset(final Long identifier) {
        companyVehicle.resetPassengers();
        companyVehicle.setOwnerId(identifier);
        companyVehicle.setMeetUpStatus(MeetUpStatus.WAITING);
        companyVehicle.setOwnerMeetUpPlace(OwnerMeetUpPlace.DEFAULT);
    }

    public void allowOwnerToStartJourney() {
        if (!companyVehicle.hasOwner()) {
            throw new InvalidVehicleStateException(companyVehicle);
        }

        if (!companyVehicle.getMeetUpStatus().equals(MeetUpStatus.WAITING)) {
            throw new InvalidVehicleStateException(companyVehicle);
        }

        companyVehicle.setMeetUpStatus(MeetUpStatus.STARTED);
    }

    public void changeOwnerMeetUpPlaceTo(final OwnerMeetUpPlace ownerMeetUpPlace) {
        if (!companyVehicle.hasOwner()) {
            throw new InvalidVehicleStateException(companyVehicle);
        }

        if (!companyVehicle.getMeetUpStatus().equals(MeetUpStatus.WAITING)) {
            throw new InvalidVehicleStateException(companyVehicle);
        }

        companyVehicle.setOwnerMeetUpPlace(ownerMeetUpPlace);
    }

    public void cancelRide() {
        if (!companyVehicle.hasOwner()) {
            throw new InvalidVehicleStateException(companyVehicle);
        }

        companyVehicle.resetRide();
    }

    public boolean isAlreadyDeparted() {
        return companyVehicle.getMeetUpStatus() != MeetUpStatus.WAITING;
    }

    void addPassengerOrUpdate(final long identifier, final MeetUpPreference preference) {
        Optional<Passenger> passenger = companyVehicle.getPassengerByIdentifier(identifier);
        passenger.ifPresentOrElse(value -> value.setMeetUpPreference(preference), () -> {
            companyVehicle.addPassenger(Passenger.builder()
                    .meetUpPreference(preference)
                    .userIdentifier(identifier)
                    .build()
            );
        });
    }

    public void addPassengerOrUpdateToOnsite(final long identifier) {
        addPassengerOrUpdate(identifier, ONSITE);
    }

    public void addPassengerToBayside(final long identifier) {
        addPassengerOrUpdate(identifier, BAYSIDE);
    }

    public void addPassengerToNone(final long identifier) {
        addPassengerOrUpdate(identifier, NONE);
    }
}
