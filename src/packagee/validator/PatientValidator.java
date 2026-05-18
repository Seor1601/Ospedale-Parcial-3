package packagee.validator;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public class PatientValidator {

    public ValidationResult validatePhone(String phone) {
        if (phone == null || !phone.matches("\\d{10}")) {
            return ValidationResult.invalid("El telefono debe tener exactamente 10 digitos.");
        }
        return ValidationResult.valid();
    }

    public ValidationResult validateEmail(String email) {
        if (email == null || !email.matches("[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.com")) {
            return ValidationResult.invalid("El email debe tener formato usuario@dominio.com.");
        }
        return ValidationResult.valid();
    }

    public ValidationResult validateBirthdate(String birthdate) {
        if (birthdate == null || !birthdate.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return ValidationResult.invalid("La fecha de nacimiento debe tener formato YYYY-MM-DD.");
        }
        try {
            LocalDate.parse(birthdate);
            return ValidationResult.valid();
        } catch (DateTimeParseException ex) {
            return ValidationResult.invalid("La fecha de nacimiento no es valida.");
        }
    }
}
