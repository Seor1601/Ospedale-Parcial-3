package packagee.controller;

import packagee.Doctor;
import packagee.Specialty;
import packagee.controller.response.Response;
import packagee.dto.JsonSerializer;
import packagee.storage.DataStore;
import packagee.validator.DoctorValidator;
import packagee.validator.UserValidator;
import packagee.validator.ValidationResult;

public class DoctorController {

    private final DataStore store;
    private final JsonSerializer serializer;
    private final UserValidator userValidator;
    private final DoctorValidator doctorValidator;

    public DoctorController() {
        this(DataStore.getInstance());
    }

    public DoctorController(DataStore store) {
        this.store = store;
        this.serializer = new JsonSerializer();
        this.userValidator = new UserValidator();
        this.doctorValidator = new DoctorValidator();
    }

    public Response registerDoctor(long id, String username, String firstname, String lastname,
            String password, String passwordConfirmation, String specialty, String licenceNumber,
            String assignedOffice, boolean isAdmin) {
        if (!isAdmin) {
            return Response.forbidden("Solo un administrador puede registrar doctores.");
        }

        Response validationResponse = validateDoctorData(id, username, password, passwordConfirmation, licenceNumber, assignedOffice);
        if (validationResponse != null) {
            return validationResponse;
        }

        Specialty parsedSpecialty = parseSpecialty(specialty);
        if (parsedSpecialty == null) {
            return Response.badRequest("La especialidad no es valida.");
        }
        if (store.findUserById(id) != null) {
            return Response.conflict("Ya existe un usuario con ese id.");
        }
        if (store.isUsernameTaken(username, -1)) {
            return Response.conflict("El nombre de usuario ya existe.");
        }

        Doctor doctor = new Doctor(id, username, firstname, lastname, password, parsedSpecialty, licenceNumber, assignedOffice);
        store.addDoctor(doctor);
        return Response.created("Doctor registrado.", serializer.doctorToJson(doctor));
    }

    public Response updateDoctor(long id, String username, String firstname, String lastname,
            String password, String passwordConfirmation, String specialty, String licenceNumber,
            String assignedOffice, boolean isAdmin) {
        if (!isAdmin) {
            return Response.forbidden("Solo un administrador puede actualizar doctores.");
        }

        Doctor doctor = store.findDoctorById(id);
        if (doctor == null) {
            return Response.notFound("Doctor no encontrado.");
        }

        Response validationResponse = validateDoctorData(id, username, password, passwordConfirmation, licenceNumber, assignedOffice);
        if (validationResponse != null) {
            return validationResponse;
        }

        Specialty parsedSpecialty = parseSpecialty(specialty);
        if (parsedSpecialty == null) {
            return Response.badRequest("La especialidad no es valida.");
        }
        if (store.isUsernameTaken(username, id)) {
            return Response.conflict("El nombre de usuario ya existe.");
        }

        doctor.setUsername(username);
        doctor.setFirstname(firstname);
        doctor.setLastname(lastname);
        doctor.setPassword(password);
        doctor.setSpecialty(parsedSpecialty);
        doctor.setLicenceNumber(licenceNumber);
        doctor.setAssignedOffice(assignedOffice);
        store.updateDoctor(doctor);

        return Response.ok("Doctor actualizado.", serializer.doctorToJson(doctor));
    }

    public Response getDoctorById(long id) {
        Doctor doctor = store.findDoctorById(id);
        if (doctor == null) {
            return Response.notFound("Doctor no encontrado.");
        }
        return Response.ok("Doctor encontrado.", serializer.doctorToJson(doctor));
    }

    public Response getAllDoctors() {
        return Response.ok("Doctores obtenidos.", serializer.doctorsToJson(store.getDoctors()));
    }

    private Response validateDoctorData(long id, String username, String password, String passwordConfirmation,
            String licenceNumber, String assignedOffice) {
        ValidationResult result = userValidator.validateId(id);
        if (!result.isValid()) {
            return Response.badRequest(result.getMessage());
        }

        result = userValidator.validateUsername(username);
        if (!result.isValid()) {
            return Response.badRequest(result.getMessage());
        }

        result = userValidator.validatePasswordConfirmation(password, passwordConfirmation);
        if (!result.isValid()) {
            return Response.badRequest(result.getMessage());
        }

        result = doctorValidator.validateLicenceNumber(licenceNumber);
        if (!result.isValid()) {
            return Response.badRequest(result.getMessage());
        }

        result = doctorValidator.validateAssignedOffice(assignedOffice);
        if (!result.isValid()) {
            return Response.badRequest(result.getMessage());
        }

        return null;
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
