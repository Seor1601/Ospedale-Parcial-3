package packagee.storage;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import packagee.Administrator;
import packagee.Appointment;
import packagee.Doctor;
import packagee.Hospitalization;
import packagee.Patient;
import packagee.User;

public class DataStore {

    private static final DataStore INSTANCE = createDefaultStore();

    private final List<Administrator> administrators;
    private final List<Patient> patients;
    private final List<Doctor> doctors;
    private final List<Appointment> appointments;
    private final List<Hospitalization> hospitalizations;

    public DataStore() {
        administrators = new ArrayList<>();
        patients = new ArrayList<>();
        doctors = new ArrayList<>();
        appointments = new ArrayList<>();
        hospitalizations = new ArrayList<>();
    }

    private static DataStore createDefaultStore() {
        DataStore store = new DataStore();
        new JsonDataLoader().loadInitialData(store);
        return store;
    }

    public static DataStore getInstance() {
        return INSTANCE;
    }

    public List<Administrator> getAdministrators() {
        return Collections.unmodifiableList(new ArrayList<>(administrators));
    }

    public List<Patient> getPatients() {
        return Collections.unmodifiableList(new ArrayList<>(patients));
    }

    public List<Doctor> getDoctors() {
        return Collections.unmodifiableList(new ArrayList<>(doctors));
    }

    public List<Appointment> getAppointments() {
        return Collections.unmodifiableList(new ArrayList<>(appointments));
    }

    public List<Hospitalization> getHospitalizations() {
        return Collections.unmodifiableList(new ArrayList<>(hospitalizations));
    }

    public Administrator findAdministratorByUsername(String username) {
        for (Administrator administrator : administrators) {
            if (administrator.getUsername().equals(username)) {
                return administrator;
            }
        }
        return null;
    }

    public Patient findPatientById(long id) {
        for (Patient patient : patients) {
            if (patient.getId() == id) {
                return patient;
            }
        }
        return null;
    }

    public Patient findPatientByUsername(String username) {
        for (Patient patient : patients) {
            if (patient.getUsername().equals(username)) {
                return patient;
            }
        }
        return null;
    }

    public Doctor findDoctorById(long id) {
        for (Doctor doctor : doctors) {
            if (doctor.getId() == id) {
                return doctor;
            }
        }
        return null;
    }

    public Doctor findDoctorByUsername(String username) {
        for (Doctor doctor : doctors) {
            if (doctor.getUsername().equals(username)) {
                return doctor;
            }
        }
        return null;
    }

    public User findUserByUsername(String username) {
        Administrator administrator = findAdministratorByUsername(username);
        if (administrator != null) {
            return administrator;
        }

        Patient patient = findPatientByUsername(username);
        if (patient != null) {
            return patient;
        }

        return findDoctorByUsername(username);
    }

    public User findUserById(long id) {
        for (Administrator administrator : administrators) {
            if (administrator.getId() == id) {
                return administrator;
            }
        }

        Patient patient = findPatientById(id);
        if (patient != null) {
            return patient;
        }

        return findDoctorById(id);
    }

    public Appointment findAppointmentById(String id) {
        for (Appointment appointment : appointments) {
            if (appointment.getId().equals(id)) {
                return appointment;
            }
        }
        return null;
    }

    public Hospitalization findHospitalizationById(String id) {
        for (Hospitalization hospitalization : hospitalizations) {
            if (hospitalization.getId().equals(id)) {
                return hospitalization;
            }
        }
        return null;
    }

    public boolean isUsernameTaken(String username, long excludedUserId) {
        User user = findUserByUsername(username);
        return user != null && user.getId() != excludedUserId;
    }

    public void addAdministrator(Administrator administrator) {
        administrators.add(administrator);
    }

    public void addPatient(Patient patient) {
        patients.add(patient);
    }

    public void addDoctor(Doctor doctor) {
        doctors.add(doctor);
    }

    public void addAppointment(Appointment appointment) {
        appointments.add(appointment);
        appointment.getPatient().addAppointment(appointment);
        appointment.getDoctor().addAppointment(appointment);
    }

    public void addHospitalization(Hospitalization hospitalization) {
        hospitalizations.add(hospitalization);
    }

    public void updatePatient(Patient patient) {
        for (int i = 0; i < patients.size(); i++) {
            if (patients.get(i).getId() == patient.getId()) {
                patients.set(i, patient);
                return;
            }
        }
    }

    public void updateDoctor(Doctor doctor) {
        for (int i = 0; i < doctors.size(); i++) {
            if (doctors.get(i).getId() == doctor.getId()) {
                doctors.set(i, doctor);
                return;
            }
        }
    }

    public void updateAppointment(Appointment appointment) {
        for (int i = 0; i < appointments.size(); i++) {
            if (appointments.get(i).getId().equals(appointment.getId())) {
                appointments.set(i, appointment);
                return;
            }
        }
    }

    public void updateHospitalization(Hospitalization hospitalization) {
        for (int i = 0; i < hospitalizations.size(); i++) {
            if (hospitalizations.get(i).getId().equals(hospitalization.getId())) {
                hospitalizations.set(i, hospitalization);
                return;
            }
        }
    }

    public List<Appointment> getAppointmentsForPatient(long patientId) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (appointment.getPatient().getId() == patientId) {
                result.add(appointment);
            }
        }
        return result;
    }

    public List<Appointment> getAppointmentsForDoctor(long doctorId) {
        List<Appointment> result = new ArrayList<>();
        for (Appointment appointment : appointments) {
            if (appointment.getDoctor().getId() == doctorId) {
                result.add(appointment);
            }
        }
        return result;
    }

    public List<Hospitalization> getHospitalizationsForPatient(long patientId) {
        List<Hospitalization> result = new ArrayList<>();
        for (Hospitalization hospitalization : hospitalizations) {
            if (hospitalization.getPatient().getId() == patientId) {
                result.add(hospitalization);
            }
        }
        return result;
    }

    public List<Hospitalization> getHospitalizationsForDoctor(long doctorId) {
        List<Hospitalization> result = new ArrayList<>();
        for (Hospitalization hospitalization : hospitalizations) {
            if (hospitalization.getDoctor().getId() == doctorId) {
                result.add(hospitalization);
            }
        }
        return result;
    }
}
