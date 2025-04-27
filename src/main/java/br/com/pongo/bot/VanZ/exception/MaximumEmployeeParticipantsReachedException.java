package br.com.pongo.bot.VanZ.exception;

import br.com.pongo.bot.VanZ.domain.CompanyVehicle;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public class MaximumEmployeeParticipantsReachedException extends RuntimeException {
    private final Long identifier;
    public MaximumEmployeeParticipantsReachedException(final Long identifier) {
        super(("Maximum employees %d was reached and could not add employee '%d'".formatted(CompanyVehicle.MAX_PASSENGERS, identifier)));
        this.identifier = identifier;
    }
}
