package packagee.validator;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import packagee.Hospitalization;

public class HospitalizationValidator {

    public ValidationResult validateDate(String date) {
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return ValidationResult.invalid("La fecha de hospitalizacion debe tener formato YYYY-MM-DD.");
        }
        try {
            LocalDate.parse(date);
            return ValidationResult.valid();
        } catch (DateTimeParseException ex) {
            return ValidationResult.invalid("La fecha de hospitalizacion no es valida.");
        }
    }

    public ValidationResult validateHospitalization(Hospitalization hospitalization) {
        if (hospitalization == null) {
            return ValidationResult.invalid("La hospitalizacion no existe.");
        }
        return ValidationResult.valid();
    }
}
