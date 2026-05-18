package packagee.validator;

public class DoctorValidator {

    public ValidationResult validateLicenceNumber(String licenceNumber) {
        if (licenceNumber == null || !licenceNumber.matches("L-\\d{10} MTL")) {
            return ValidationResult.invalid("La licencia debe cumplir L-XXXXXXXXXX MTL.");
        }
        return ValidationResult.valid();
    }

    public ValidationResult validateAssignedOffice(String assignedOffice) {
        if (assignedOffice == null || !assignedOffice.matches("O-\\d{3}")) {
            return ValidationResult.invalid("La oficina debe cumplir O-XXX.");
        }
        return ValidationResult.valid();
    }
}
