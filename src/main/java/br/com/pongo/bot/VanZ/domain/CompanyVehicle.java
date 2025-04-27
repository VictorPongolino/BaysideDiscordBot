package br.com.pongo.bot.VanZ.domain;

import br.com.pongo.bot.VanZ.domain.CompanyVehicle.Passenger.MeetUpPreference;
import br.com.pongo.bot.VanZ.exception.MaximumEmployeeParticipantsReachedException;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

@Getter @Setter
@Component
public final class CompanyVehicle {

    public static final byte MAX_PASSENGERS = 4;
    private static final Passenger NO_EMPLOYEE = null;
    private static final Long NO_OWNER = null;

    private Long ownerId;
    private MeetUpStatus meetUpStatus;
    private OwnerMeetUpPlace ownerMeetUpPlace;
    private final Passenger[] passengers = new Passenger[MAX_PASSENGERS];

    public CompanyVehicle() {
        resetPassengers();
        meetUpStatus = MeetUpStatus.NONE;
        ownerMeetUpPlace = OwnerMeetUpPlace.ABORTED;
        ownerId = NO_OWNER;
    }

    public void resetRide() {
        this.ownerId = NO_OWNER;
        meetUpStatus = MeetUpStatus.NONE;
        ownerMeetUpPlace = OwnerMeetUpPlace.ABORTED;
        resetPassengers();
    }

    public boolean hasOwner() {
        return ownerId != NO_OWNER;
    }

    void addPassenger(final byte slot, final Passenger passenger) {
        passengers[slot] = passenger;
    }

    public boolean hasPassenger(final long identifier) {
        for(byte i = 0; i < MAX_PASSENGERS; i++) {
            if (hasPassengerSlot(i)) {
                if (passengers[i].userIdentifier == identifier) {
                    return true;
                }
            }
        }

        return false;
    }

    public void removePassenger(final long identifier) {
        for(byte i = 0; i < MAX_PASSENGERS; i++) {
            if (hasPassengerSlot(i)) {
                if (passengers[i].userIdentifier == identifier) {
                    passengers[i] = NO_EMPLOYEE;
                    return;
                }
            }
        }
    }

    public void addPassenger(final Passenger passenger) {
        for(byte i = 0; i < MAX_PASSENGERS; i++) {
            if (!hasPassengerSlot(i)) {
                addPassenger(i, passenger);
                return;
            }
        }

        throw new MaximumEmployeeParticipantsReachedException(passenger.userIdentifier);
    }

    public Optional<Passenger> getPassengerByIdentifier(long identifier) {
        return Stream.of(passengers)
                .filter(user -> user != null && user.userIdentifier != null && user.userIdentifier.equals(identifier))
                .findFirst();
    }

    private boolean hasPassengerSlot(int i) {
        return passengers[i] != NO_EMPLOYEE;
    }

    public void setMeetUpStatus(final MeetUpStatus meetUpStatus) {
        Objects.requireNonNull(meetUpStatus, "Meet UP status must not be null!");
        this.meetUpStatus = meetUpStatus;
    }

    public OwnerMeetUpPlace getOwnerMeetUpPlace() {
        return ownerMeetUpPlace;
    }

    public void setOwnerMeetUpPlace(final OwnerMeetUpPlace ownerMeetUpPlace) {
        this.ownerMeetUpPlace = ownerMeetUpPlace;
    }

    public void resetPassengers() {
        Arrays.fill(passengers, NO_EMPLOYEE);
    }

    public List<Passenger> getPassengersByMeetUp(final MeetUpPreference meetUpPreference) {
        return Stream.of(passengers)
                .filter(passenger -> passenger != null && passenger.getMeetUpPreference() != null && passenger.getMeetUpPreference().equals(meetUpPreference)).toList();
    }

    public boolean isActive() {
        return hasOwner() && !ownerMeetUpPlace.equals(OwnerMeetUpPlace.ABORTED);
    }

    public boolean isAborted() {
        return !ownerMeetUpPlace.equals(OwnerMeetUpPlace.ABORTED);
    }

    public enum OwnerMeetUpPlace {
        DEFAULT, ONSITE, BAYSIDE, ABORTED
    }

    @Builder @Getter
    public static final class Passenger {
        private Long userIdentifier;
        private MeetUpPreference meetUpPreference;

        public void setMeetUpPreference(final MeetUpPreference meetUpPreference) {
            this.meetUpPreference = meetUpPreference;
        }

        public enum MeetUpPreference {
            PENDING, ONSITE, BAYSIDE, NONE
        }
    }

    public enum MeetUpStatus {
        WAITING, STARTED, CONCLUDED, NONE
    }
}

