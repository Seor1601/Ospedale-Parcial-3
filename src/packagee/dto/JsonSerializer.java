package packagee.dto;

import java.util.List;
import org.json.JSONArray;
import org.json.JSONObject;
import packagee.Administrator;
import packagee.Appointment;
import packagee.Doctor;
import packagee.Hospitalization;
import packagee.Patient;
import packagee.Prescription;
import packagee.User;

public class JsonSerializer {

    public String userToJson(User user, String role) {
        JSONObject json = basicUserJson(user);
        json.put("role", role);

        if (user instanceof Patient patient) {
            addPatientFields(json, patient);
        } else if (user instanceof Doctor doctor) {
            addDoctorFields(json, doctor);
        } else if (user instanceof Administrator) {
            json.put("type", "ADMIN");
        }

        return json.toString();
    }

    public String patientToJson(Patient patient) {
        JSONObject json = basicUserJson(patient);
        addPatientFields(json, patient);
        return json.toString();
    }

    public String doctorToJson(Doctor doctor) {
        JSONObject json = basicUserJson(doctor);
        addDoctorFields(json, doctor);
        return json.toString();
    }

    public String appointmentToJson(Appointment appointment) {
        return appointmentJsonObject(appointment).toString();
    }

    public String hospitalizationToJson(Hospitalization hospitalization) {
        return hospitalizationJsonObject(hospitalization).toString();
    }

    public String patientsToJson(List<Patient> patients) {
        JSONArray array = new JSONArray();
        for (Patient patient : patients) {
            array.put(new JSONObject(patientToJson(patient)));
        }
        return array.toString();
    }

    public String doctorsToJson(List<Doctor> doctors) {
        JSONArray array = new JSONArray();
        for (Doctor doctor : doctors) {
            array.put(new JSONObject(doctorToJson(doctor)));
        }
        return array.toString();
    }

    public String appointmentsToJson(List<Appointment> appointments) {
        JSONArray array = new JSONArray();
        for (Appointment appointment : appointments) {
            array.put(appointmentJsonObject(appointment));
        }
        return array.toString();
    }

    public String hospitalizationsToJson(List<Hospitalization> hospitalizations) {
        JSONArray array = new JSONArray();
        for (Hospitalization hospitalization : hospitalizations) {
            array.put(hospitalizationJsonObject(hospitalization));
        }
        return array.toString();
    }

    private JSONObject basicUserJson(User user) {
        JSONObject json = new JSONObject();
        json.put("id", user.getId());
        json.put("username", user.getUsername());
        json.put("firstname", user.getFirstname());
        json.put("lastname", user.getLastname());
        return json;
    }

    private void addPatientFields(JSONObject json, Patient patient) {
        json.put("type", "PATIENT");
        json.put("email", patient.getEmail());
        json.put("birthdate", patient.getBirthdate().toString());
        json.put("gender", patient.isGender());
        json.put("phone", patient.getPhone());
        json.put("address", patient.getAddress());
    }

    private void addDoctorFields(JSONObject json, Doctor doctor) {
        json.put("type", "DOCTOR");
        json.put("specialty", doctor.getSpecialty().name());
        json.put("licenceNumber", doctor.getLicenceNumber());
        json.put("assignedOffice", doctor.getAssignedOffice());
    }

    private JSONObject appointmentJsonObject(Appointment appointment) {
        JSONObject json = new JSONObject();
        json.put("id", appointment.getId());
        json.put("patientId", appointment.getPatient().getId());
        json.put("patientName", appointment.getPatient().getFirstname() + " " + appointment.getPatient().getLastname());
        json.put("doctorId", appointment.getDoctor().getId());
        json.put("doctorName", appointment.getDoctor().getFirstname() + " " + appointment.getDoctor().getLastname());
        json.put("specialty", appointment.getSpecialty().name());
        json.put("datetime", appointment.getDatetime().toString());
        json.put("reason", appointment.getReason());
        json.put("type", appointment.isType() ? "IN_PERSON" : "REMOTE");
        json.put("status", appointment.getStatus().name());
        json.put("diagnosis", valueOrNull(appointment.getDiagnosis()));
        json.put("observations", valueOrNull(appointment.getObservations()));
        json.put("recommendedTreatment", valueOrNull(appointment.getRecommendedTreatment()));
        json.put("followUp", valueOrNull(appointment.getFollowUp()));

        JSONArray prescriptions = new JSONArray();
        for (Prescription prescription : appointment.getPrescriptions()) {
            JSONObject item = new JSONObject();
            item.put("medicationName", prescription.getMedicationName());
            item.put("dose", prescription.getDose());
            item.put("administrationRoute", prescription.getAdministrationRoute());
            item.put("treatmentDuration", prescription.getTreatmentDuration());
            item.put("additionalInstructions", prescription.getAdditionalInstructions());
            item.put("frecuency", prescription.getFrecuency());
            prescriptions.put(item);
        }
        json.put("prescriptions", prescriptions);

        return json;
    }

    private JSONObject hospitalizationJsonObject(Hospitalization hospitalization) {
        JSONObject json = new JSONObject();
        json.put("id", hospitalization.getId());
        json.put("patientId", hospitalization.getPatient().getId());
        json.put("patientName", hospitalization.getPatient().getFirstname() + " " + hospitalization.getPatient().getLastname());
        json.put("doctorId", hospitalization.getDoctor().getId());
        json.put("doctorName", hospitalization.getDoctor().getFirstname() + " " + hospitalization.getDoctor().getLastname());
        json.put("date", hospitalization.getDate().toString());
        json.put("reason", hospitalization.getReason());
        json.put("roomType", hospitalization.getRoomType().name());
        json.put("observations", hospitalization.getObservations());
        json.put("status", hospitalization.getStatus().name());
        return json;
    }

    private Object valueOrNull(String value) {
        return value == null ? JSONObject.NULL : value;
    }
}
