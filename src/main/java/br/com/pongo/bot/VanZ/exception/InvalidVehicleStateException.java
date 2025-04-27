package br.com.pongo.bot.VanZ.exception;

import br.com.pongo.bot.VanZ.domain.CompanyVehicle;
import lombok.Getter;
import lombok.ToString;

@Getter @ToString
public class InvalidVehicleStateException extends RuntimeException {
    private final CompanyVehicle companyVehicle;
    public InvalidVehicleStateException(final CompanyVehicle companyVehicle) {
        super("Invalid Operation doesn't reflect for the current vehicle state");
        this.companyVehicle = companyVehicle;
    }
}
