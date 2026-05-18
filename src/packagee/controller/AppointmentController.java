package packagee.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import packagee.Appointment;
import packagee.AppointmentStatus;
import packagee.Doctor;
import packagee.Patient;
import packagee.Prescription;
import packagee.Specialty;
import packagee.controller.response.Response;
import packagee.dto.JsonSerializer;
import packagee.storage.DataStore;
import packagee.storage.IdGenerator;
import packagee.validator.AppointmentValidator;
import packagee.validator.ValidationResult;

public class AppointmentController {

    private final DataStore store;
    private final JsonSerializer serializer;
    private final IdGenerator idGenerator;
    private final AppointmentValidator validator;

    public AppointmentController() {
        this(DataStore.getInstance());
    }

    public AppointmentController(DataStore store) {
        this.store = store;
        this.serializer = new JsonSerializer();
        this.idGenerator = new IdGenerator();
        this.validator = new AppointmentValidator();
    }

    public Response requestAppointmentByDoctor(long patientId, long doctorId, String date, String time,
            String reason, boolean type) {
        Patient patient = store.findPatientById(patientId);
        if (patient == null) {
            return Response.notFound("Paciente no encontrado.");
        }

        Doctor doctor = store.findDoctorById(doctorId);
        ValidationResult doctorValidation = validator.validateDoctor(doctor);
        if (!doctorValidation.isValid()) {
            return Response.notFound(doctorValidation.getMessage());
        }

        Response dateTimeValidation = validateDateTime(date, time);
        if (dateTimeValidation != null) {
            return dateTimeValidation;
        }

        LocalDateTime datetime = LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time));
        if (!isDoctorAvailable(doctor, datetime, null)) {
            return Response.conflict("El doctor no esta disponible en ese horario.");
        }

        String id = idGenerator.nextAppointmentId(patientId, store);
        Appointment appointment = new Appointment(id, patient, doctor, doctor.getSpecialty(), datetime, reason, type);
        store.addAppointment(appointment);
        return Response.created("Cita solicitada.", serializer.appointmentToJson(appointment));
    }

    public Response requestAppointmentBySpecialty(long patientId, String specialty, String date, String time,
            String reason, boolean type) {
        Patient patient = store.findPatientById(patientId);
        if (patient == null) {
            return Response.notFound("Paciente no encontrado.");
        }

        Specialty parsedSpecialty = parseSpecialty(specialty);
        if (parsedSpecialty == null) {
            return Response.badRequest("La especialidad no es valida.");
        }

        Response dateTimeValidation = validateDateTime(date, time);
        if (dateTimeValidation != null) {
            return dateTimeValidation;
        }

        LocalDateTime datetime = LocalDateTime.of(LocalDate.parse(date), LocalTime.parse(time));
        Doctor selectedDoctor = null;
        for (Doctor doctor : store.getDoctors()) {
            if (doctor.getSpecialty() == parsedSpecialty && isDoctorAvailable(doctor, datetime, null)) {
                selectedDoctor = doctor;
                break;
            }
        }

        if (selectedDoctor == null) {
            return Response.conflict("No hay doctores disponibles para esa especialidad y horario.");
        }

        String id = idGenerator.nextAppointmentId(patientId, store);
        Appointment appointment = new Appointment(id, patient, selectedDoctor, parsedSpecialty, datetime, reason, type);
        store.addAppointment(appointment);
        return Response.created("Cita solicitada.", serializer.appointmentToJson(appointment));
    }

    public Response acceptAppointment(String appointmentId, long doctorId) {
        Appointment appointment = store.findAppointmentById(appointmentId);
        ValidationResult appointmentValidation = validator.validateAppointment(appointment);
        if (!appointmentValidation.isValid()) {
            return Response.notFound(appointmentValidation.getMessage());
        }
        if (appointment.getDoctor().getId() != doctorId) {
            return Response.forbidden("La cita no pertenece a este doctor.");
        }
        if (appointment.getStatus() != AppointmentStatus.REQUESTED) {
            return Response.conflict("Solo se pueden aceptar citas solicitadas.");
        }

        appointment.setStatus(AppointmentStatus.PENDING);
        store.updateAppointment(appointment);
        return Response.ok("Cita aceptada.", serializer.appointmentToJson(appointment));
    }

    public Response completeAppointment(String appointmentId, long doctorId, String diagnosis,
            String observations, String recommendedTreatment, String followUp) {
        Appointment appointment = store.findAppointmentById(appointmentId);
        ValidationResult appointmentValidation = validator.validateAppointment(appointment);
        if (!appointmentValidation.isValid()) {
            return Response.notFound(appointmentValidation.getMessage());
        }
        if (appointment.getDoctor().getId() != doctorId) {
            return Response.forbidden("La cita no pertenece a este doctor.");
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            return Response.conflict("Solo se pueden completar citas pendientes.");
        }

        appointment.setDiagnosis(diagnosis);
        appointment.setObservations(observations);
        appointment.setRecommendedTreatment(recommendedTreatment);
        appointment.setFollowUp(followUp);
        appointment.setStatus(AppointmentStatus.COMPLETED);
        store.updateAppointment(appointment);
        return Response.ok("Cita completada.", serializer.appointmentToJson(appointment));
    }

    public Response cancelAppointment(String appointmentId, long patientId) {
        Appointment appointment = store.findAppointmentById(appointmentId);
        ValidationResult appointmentValidation = validator.validateAppointment(appointment);
        if (!appointmentValidation.isValid()) {
            return Response.notFound(appointmentValidation.getMessage());
        }
        if (appointment.getPatient().getId() != patientId) {
            return Response.forbidden("La cita no pertenece a este paciente.");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED) {
            return Response.conflict("No se puede cancelar una cita completada.");
        }

        appointment.setStatus(AppointmentStatus.CANCELED);
        store.updateAppointment(appointment);
        return Response.ok("Cita cancelada.", serializer.appointmentToJson(appointment));
    }

    public Response rescheduleAppointment(String appointmentId, long doctorId, String newTime, String reason) {
        Appointment appointment = store.findAppointmentById(appointmentId);
        ValidationResult appointmentValidation = validator.validateAppointment(appointment);
        if (!appointmentValidation.isValid()) {
            return Response.notFound(appointmentValidation.getMessage());
        }
        if (appointment.getDoctor().getId() != doctorId) {
            return Response.forbidden("La cita no pertenece a este doctor.");
        }
        if (appointment.getStatus() == AppointmentStatus.COMPLETED || appointment.getStatus() == AppointmentStatus.CANCELED) {
            return Response.conflict("No se puede reprogramar una cita finalizada.");
        }

        ValidationResult timeValidation = validator.validateTime(newTime);
        if (!timeValidation.isValid()) {
            return Response.badRequest(timeValidation.getMessage());
        }

        LocalDateTime newDateTime = LocalDateTime.of(appointment.getDatetime().toLocalDate(), LocalTime.parse(newTime));
        if (!isDoctorAvailable(appointment.getDoctor(), newDateTime, appointment.getId())) {
            return Response.conflict("El doctor no esta disponible en ese horario.");
        }

        appointment.setDatetime(newDateTime);
        appointment.setReason(appendRescheduleReason(appointment.getReason(), reason));
        store.updateAppointment(appointment);
        return Response.ok("Cita reprogramada.", serializer.appointmentToJson(appointment));
    }

    public Response prescribeMedication(String appointmentId, long doctorId, String medicationName,
            double dose, String administrationRoute, int treatmentDuration, String additionalInstructions,
            int frecuency) {
        Appointment appointment = store.findAppointmentById(appointmentId);
        ValidationResult appointmentValidation = validator.validateAppointment(appointment);
        if (!appointmentValidation.isValid()) {
            return Response.notFound(appointmentValidation.getMessage());
        }
        if (appointment.getDoctor().getId() != doctorId) {
            return Response.forbidden("La cita no pertenece a este doctor.");
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            return Response.conflict("Solo se pueden prescribir medicamentos en citas pendientes.");
        }
        if (medicationName == null || medicationName.trim().isEmpty()
                || administrationRoute == null || administrationRoute.trim().isEmpty()
                || dose <= 0 || treatmentDuration <= 0 || frecuency <= 0) {
            return Response.badRequest("Los datos de la prescripcion no son validos.");
        }

        new Prescription(appointment, medicationName, dose, administrationRoute, treatmentDuration,
                additionalInstructions, frecuency);
        store.updateAppointment(appointment);
        return Response.ok("Medicamento prescrito.", serializer.appointmentToJson(appointment));
    }

    public Response getPatientAppointments(long patientId) {
        if (store.findPatientById(patientId) == null) {
            return Response.notFound("Paciente no encontrado.");
        }

        List<Appointment> appointments = new ArrayList<>(store.getAppointmentsForPatient(patientId));
        appointments.sort(Comparator.comparing(Appointment::getDatetime).reversed());
        return Response.ok("Citas del paciente obtenidas.", serializer.appointmentsToJson(appointments));
    }

    public Response getDoctorAppointments(long doctorId, boolean onlyPending) {
        if (store.findDoctorById(doctorId) == null) {
            return Response.notFound("Doctor no encontrado.");
        }

        List<Appointment> appointments = new ArrayList<>();
        for (Appointment appointment : store.getAppointmentsForDoctor(doctorId)) {
            if (!onlyPending || appointment.getStatus() == AppointmentStatus.PENDING) {
                appointments.add(appointment);
            }
        }
        appointments.sort(Comparator.comparing(Appointment::getDatetime).reversed());
        return Response.ok("Citas del doctor obtenidas.", serializer.appointmentsToJson(appointments));
    }

    private Response validateDateTime(String date, String time) {
        ValidationResult dateValidation = validator.validateDate(date);
        if (!dateValidation.isValid()) {
            return Response.badRequest(dateValidation.getMessage());
        }

        ValidationResult timeValidation = validator.validateTime(time);
        if (!timeValidation.isValid()) {
            return Response.badRequest(timeValidation.getMessage());
        }

        return null;
    }

    private boolean isDoctorAvailable(Doctor doctor, LocalDateTime datetime, String ignoredAppointmentId) {
        for (Appointment appointment : store.getAppointmentsForDoctor(doctor.getId())) {
            if (ignoredAppointmentId != null && appointment.getId().equals(ignoredAppointmentId)) {
                continue;
            }
            if (appointment.getStatus() != AppointmentStatus.CANCELED && appointment.getDatetime().equals(datetime)) {
                return false;
            }
        }
        return true;
    }

    private String appendRescheduleReason(String currentReason, String newReason) {
        String safeCurrent = currentReason == null ? "" : currentReason;
        String safeNew = newReason == null ? "" : newReason;
        if (safeCurrent.isEmpty()) {
            return "Reprogramada: " + safeNew;
        }
        return safeCurrent + " | Reprogramada: " + safeNew;
    }

    private Specialty parseSpecialty(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim().toUpperCase().replace(" ", "_").replace("&", "AND");
        if ("ORTHOPEDICS".equals(normalized)) {
            return Specialty.TRAUMATOLOGY_ORTHOPEDICS;
        }
        if ("GYNECOLOGY".equals(normalized)) {
            return Specialty.GYNECOLOGY_OBSTETRICS;
        }
        if ("TRAUMATOLOGY_AND_ORTHOPEDICS".equals(normalized)) {
            return Specialty.TRAUMATOLOGY_ORTHOPEDICS;
        }
        if ("GYNECOLOGY_AND_OBSTETRICS".equals(normalized)) {
            return Specialty.GYNECOLOGY_OBSTETRICS;
        }

        try {
            return Specialty.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
