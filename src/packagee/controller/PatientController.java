package packagee.controller;

import java.time.LocalDate;
import packagee.Patient;
import packagee.controller.response.Response;
import packagee.dto.JsonSerializer;
import packagee.storage.DataStore;
import packagee.validator.PatientValidator;
import packagee.validator.UserValidator;
import packagee.validator.ValidationResult;

public class PatientController {

    private final DataStore store;
    private final JsonSerializer serializer;
    private final UserValidator userValidator;
    private final PatientValidator patientValidator;

    public PatientController() {
        this(DataStore.getInstance());
    }

    public PatientController(DataStore store) {
        this.store = store;
        this.serializer = new JsonSerializer();
        this.userValidator = new UserValidator();
        this.patientValidator = new PatientValidator();
    }

    public Response registerPatient(long id, String username, String firstname, String lastname,
            String password, String passwordConfirmation, String email, String birthdate,
            boolean gender, String phone, String address) {
        Response validationResponse = validatePatientData(id, username, password, passwordConfirmation, email, birthdate, phone);
        if (validationResponse != null) {
            return validationResponse;
        }

        if (store.findUserById(id) != null) {
            return Response.conflict("Ya existe un usuario con ese id.");
        }
        if (store.isUsernameTaken(username, -1)) {
            return Response.conflict("El nombre de usuario ya existe.");
        }

        Patient patient = new Patient(
                id,
                username,
                firstname,
                lastname,
                password,
                email,
                LocalDate.parse(birthdate),
                gender,
                Long.parseLong(phone),
                address
        );
        store.addPatient(patient);
        return Response.created("Paciente registrado.", serializer.patientToJson(patient));
    }

    public Response updatePatient(long id, String username, String firstname, String lastname,
            String password, String passwordConfirmation, String email, String birthdate,
            boolean gender, String phone, String address) {
        Patient patient = store.findPatientById(id);
        if (patient == null) {
            return Response.notFound("Paciente no encontrado.");
        }

        Response validationResponse = validatePatientData(id, username, password, passwordConfirmation, email, birthdate, phone);
        if (validationResponse != null) {
            return validationResponse;
        }
        if (store.isUsernameTaken(username, id)) {
            return Response.conflict("El nombre de usuario ya existe.");
        }

        patient.setUsername(username);
        patient.setFirstname(firstname);
        patient.setLastname(lastname);
        patient.setPassword(password);
        patient.setEmail(email);
        patient.setBirthdate(LocalDate.parse(birthdate));
        patient.setGender(gender);
        patient.setPhone(Long.parseLong(phone));
        patient.setAddress(address);
        store.updatePatient(patient);

        return Response.ok("Paciente actualizado.", serializer.patientToJson(patient));
    }

    public Response getPatientById(long id) {
        Patient patient = store.findPatientById(id);
        if (patient == null) {
            return Response.notFound("Paciente no encontrado.");
        }
        return Response.ok("Paciente encontrado.", serializer.patientToJson(patient));
    }

    public Response getAllPatients() {
        return Response.ok("Pacientes obtenidos.", serializer.patientsToJson(store.getPatients()));
    }

    private Response validatePatientData(long id, String username, String password, String passwordConfirmation,
            String email, String birthdate, String phone) {
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

        result = patientValidator.validatePhone(phone);
        if (!result.isValid()) {
            return Response.badRequest(result.getMessage());
        }

        result = patientValidator.validateEmail(email);
        if (!result.isValid()) {
            return Response.badRequest(result.getMessage());
        }

        result = patientValidator.validateBirthdate(birthdate);
        if (!result.isValid()) {
            return Response.badRequest(result.getMessage());
        }

        return null;
    }
}
