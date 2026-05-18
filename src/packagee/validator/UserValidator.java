package packagee.validator;

public class UserValidator {

    public ValidationResult validateId(long id) {
        if (id <= 0) {
            return ValidationResult.invalid("El id debe ser mayor que 0.");
        }
        if (!String.valueOf(id).matches("\\d{12}")) {
            return ValidationResult.invalid("El id debe tener exactamente 12 digitos.");
        }
        return ValidationResult.valid();
    }

    public ValidationResult validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return ValidationResult.invalid("El nombre de usuario es obligatorio.");
        }
        return ValidationResult.valid();
    }

    public ValidationResult validatePasswordConfirmation(String password, String confirmation) {
        if (password == null || confirmation == null || !password.equals(confirmation)) {
            return ValidationResult.invalid("La contrasena y la confirmacion deben coincidir.");
        }
        return ValidationResult.valid();
    }
}
