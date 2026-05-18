package packagee.controller;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import packagee.Appointment;
import packagee.AppointmentStatus;
import packagee.Doctor;
import packagee.Hospitalization;
import packagee.HospitalizationStatus;
import packagee.Patient;
import packagee.RoomType;
import packagee.controller.response.Response;
import packagee.dto.JsonSerializer;
import packagee.storage.DataStore;
import packagee.storage.IdGenerator;
import packagee.validator.HospitalizationValidator;
import packagee.validator.ValidationResult;

public class HospitalizationController {

    private final DataStore store;
    private final JsonSerializer serializer;
    private final IdGenerator idGenerator;
    private final HospitalizationValidator validator;

    public HospitalizationController() {
        this(DataStore.getInstance());
    }

    public HospitalizationController(DataStore store) {
        this.store = store;
        this.serializer = new JsonSerializer();
        this.idGenerator = new IdGenerator();
        this.validator = new HospitalizationValidator();
    }

    public Response requestHospitalization(long patientId, long doctorId, String date, String reason,
            String roomType, String observations) {
        Patient patient = store.findPatientById(patientId);
        if (patient == null) {
            return Response.notFound("Paciente no encontrado.");
        }

        Doctor doctor = store.findDoctorById(doctorId);
        if (doctor == null) {
            return Response.notFound("Doctor no encontrado.");
        }

        Response validationResponse = validateHospitalizationData(date, roomType);
        if (validationResponse != null) {
            return validationResponse;
        }

        Hospitalization hospitalization = new Hospitalization(
                idGenerator.nextHospitalizationId(patientId, store),
                patient,
                doctor,
                LocalDate.parse(date),
                reason,
                RoomType.valueOf(roomType.trim().toUpperCase()),
                observations
        );
        store.addHospitalization(hospitalization);
        return Response.created("Hospitalizacion solicitada.", serializer.hospitalizationToJson(hospitalization));
    }

    public Response approveHospitalization(String hospitalizationId, long doctorId) {
        Hospitalization hospitalization = store.findHospitalizationById(hospitalizationId);
        ValidationResult hospitalizationValidation = validator.validateHospitalization(hospitalization);
        if (!hospitalizationValidation.isValid()) {
            return Response.notFound(hospitalizationValidation.getMessage());
        }
        if (hospitalization.getDoctor().getId() != doctorId) {
            return Response.forbidden("La hospitalizacion no pertenece a este doctor.");
        }
        if (hospitalization.getStatus() != HospitalizationStatus.REQUESTED) {
            return Response.conflict("Solo se pueden aprobar hospitalizaciones solicitadas.");
        }

        hospitalization.setStatus(HospitalizationStatus.ONGOING);
        store.updateHospitalization(hospitalization);
        return Response.ok("Hospitalizacion aprobada.", serializer.hospitalizationToJson(hospitalization));
    }

    public Response denyHospitalization(String hospitalizationId, long doctorId) {
        Hospitalization hospitalization = store.findHospitalizationById(hospitalizationId);
        ValidationResult hospitalizationValidation = validator.validateHospitalization(hospitalization);
        if (!hospitalizationValidation.isValid()) {
            return Response.notFound(hospitalizationValidation.getMessage());
        }
        if (hospitalization.getDoctor().getId() != doctorId) {
            return Response.forbidden("La hospitalizacion no pertenece a este doctor.");
        }
        if (hospitalization.getStatus() != HospitalizationStatus.REQUESTED) {
            return Response.conflict("Solo se pueden denegar hospitalizaciones solicitadas.");
        }

        hospitalization.setStatus(HospitalizationStatus.CANCELED);
        store.updateHospitalization(hospitalization);
        return Response.ok("Hospitalizacion denegada.", serializer.hospitalizationToJson(hospitalization));
    }

    public Response sendPatientToHospitalizationFromAppointment(String appointmentId, long doctorId,
            String date, String reason, String roomType, String observations) {
        Appointment appointment = store.findAppointmentById(appointmentId);
        if (appointment == null) {
            return Response.notFound("La cita no existe.");
        }
        if (appointment.getDoctor().getId() != doctorId) {
            return Response.forbidden("La cita no pertenece a este doctor.");
        }
        if (appointment.getStatus() != AppointmentStatus.PENDING) {
            return Response.conflict("Solo se puede hospitalizar desde una cita pendiente.");
        }

        Response validationResponse = validateHospitalizationData(date, roomType);
        if (validationResponse != null) {
            return validationResponse;
        }

        Hospitalization hospitalization = new Hospitalization(
                idGenerator.nextHospitalizationId(appointment.getPatient().getId(), store),
                appointment.getPatient(),
                appointment.getDoctor(),
                LocalDate.parse(date),
                reason,
                RoomType.valueOf(roomType.trim().toUpperCase()),
                observations,
                HospitalizationStatus.ONGOING
        );
        appointment.setStatus(AppointmentStatus.COMPLETED);
        store.addHospitalization(hospitalization);
        store.updateAppointment(appointment);
        return Response.created("Paciente enviado a hospitalizacion.", serializer.hospitalizationToJson(hospitalization));
    }

    public Response cancelHospitalization(String hospitalizationId, long patientId) {
        Hospitalization hospitalization = store.findHospitalizationById(hospitalizationId);
        ValidationResult hospitalizationValidation = validator.validateHospitalization(hospitalization);
        if (!hospitalizationValidation.isValid()) {
            return Response.notFound(hospitalizationValidation.getMessage());
        }
        if (hospitalization.getPatient().getId() != patientId) {
            return Response.forbidden("La hospitalizacion no pertenece a este paciente.");
        }
        if (hospitalization.getStatus() == HospitalizationStatus.ONGOING) {
            return Response.conflict("No se puede cancelar una hospitalizacion en curso.");
        }

        hospitalization.setStatus(HospitalizationStatus.CANCELED);
        store.updateHospitalization(hospitalization);
        return Response.ok("Hospitalizacion cancelada.", serializer.hospitalizationToJson(hospitalization));
    }

    public Response getPatientHospitalizations(long patientId) {
        if (store.findPatientById(patientId) == null) {
            return Response.notFound("Paciente no encontrado.");
        }

        List<Hospitalization> hospitalizations = new ArrayList<>(store.getHospitalizationsForPatient(patientId));
        hospitalizations.sort(Comparator.comparing(Hospitalization::getDate).reversed());
        return Response.ok("Hospitalizaciones del paciente obtenidas.", serializer.hospitalizationsToJson(hospitalizations));
    }

    public Response getDoctorHospitalizations(long doctorId) {
        if (store.findDoctorById(doctorId) == null) {
            return Response.notFound("Doctor no encontrado.");
        }

        List<Hospitalization> hospitalizations = new ArrayList<>(store.getHospitalizationsForDoctor(doctorId));
        hospitalizations.sort(Comparator.comparing(Hospitalization::getDate).reversed());
        return Response.ok("Hospitalizaciones del doctor obtenidas.", serializer.hospitalizationsToJson(hospitalizations));
    }

    private Response validateHospitalizationData(String date, String roomType) {
        ValidationResult dateValidation = validator.validateDate(date);
        if (!dateValidation.isValid()) {
            return Response.badRequest(dateValidation.getMessage());
        }

        if (parseRoomType(roomType) == null) {
            return Response.badRequest("El tipo de habitacion no es valido.");
        }
        return null;
    }

    private RoomType parseRoomType(String roomType) {
        if (roomType == null) {
            return null;
        }
        try {
            return RoomType.valueOf(roomType.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
