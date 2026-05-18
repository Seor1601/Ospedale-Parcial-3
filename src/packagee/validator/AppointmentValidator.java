package packagee.validator;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import packagee.Appointment;
import packagee.Doctor;

public class AppointmentValidator {

    public ValidationResult validateDate(String date) {
        if (date == null || !date.matches("\\d{4}-\\d{2}-\\d{2}")) {
            return ValidationResult.invalid("La fecha de la cita debe tener formato YYYY-MM-DD.");
        }
        try {
            LocalDate.parse(date);
            return ValidationResult.valid();
        } catch (DateTimeParseException ex) {
            return ValidationResult.invalid("La fecha de la cita no es valida.");
        }
    }

    public ValidationResult validateTime(String time) {
        if (time == null || !time.matches("\\d{2}:\\d{2}")) {
            return ValidationResult.invalid("La hora de la cita debe tener formato HH:mm.");
        }
        try {
            LocalTime parsedTime = LocalTime.parse(time);
            int minute = parsedTime.getMinute();
            if (minute != 0 && minute != 15 && minute != 30 && minute != 45) {
                return ValidationResult.invalid("La cita debe iniciar en minutos 00, 15, 30 o 45.");
            }
            return ValidationResult.valid();
        } catch (DateTimeParseException ex) {
            return ValidationResult.invalid("La hora de la cita no es valida.");
        }
    }

    public ValidationResult validateDoctor(Doctor doctor) {
        if (doctor == null) {
            return ValidationResult.invalid("El doctor no existe.");
        }
        return ValidationResult.valid();
    }

    public ValidationResult validateAppointment(Appointment appointment) {
        if (appointment == null) {
            return ValidationResult.invalid("La cita no existe.");
        }
        return ValidationResult.valid();
    }
}
