package packagee.controller;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import packagee.Appointment;
import packagee.AppointmentStatus;
import packagee.Hospitalization;
import packagee.controller.response.Response;
import packagee.dto.JsonSerializer;
import packagee.storage.DataStore;

public class TableController {

    private final DataStore store;
    private final JsonSerializer serializer;

    public TableController() {
        this(DataStore.getInstance());
    }

    public TableController(DataStore store) {
        this.store = store;
        this.serializer = new JsonSerializer();
    }

    public Response getPatientsTableData() {
        return Response.ok("Datos de pacientes obtenidos.", serializer.patientsToJson(store.getPatients()));
    }

    public Response getDoctorsTableData() {
        return Response.ok("Datos de doctores obtenidos.", serializer.doctorsToJson(store.getDoctors()));
    }

    public Response getAppointmentsTableData() {
        List<Appointment> appointments = new ArrayList<>(store.getAppointments());
        appointments.sort(Comparator.comparing(Appointment::getDatetime).reversed());
        return Response.ok("Datos de citas obtenidos.", serializer.appointmentsToJson(appointments));
    }

    public Response getHospitalizationsTableData() {
        List<Hospitalization> hospitalizations = new ArrayList<>(store.getHospitalizations());
        hospitalizations.sort(Comparator.comparing(Hospitalization::getDate).reversed());
        return Response.ok("Datos de hospitalizaciones obtenidos.", serializer.hospitalizationsToJson(hospitalizations));
    }

    public Response getPatientAppointmentsTableData(long patientId) {
        if (store.findPatientById(patientId) == null) {
            return Response.notFound("Paciente no encontrado.");
        }

        List<Appointment> appointments = new ArrayList<>(store.getAppointmentsForPatient(patientId));
        appointments.sort(Comparator.comparing(Appointment::getDatetime).reversed());
        return Response.ok("Datos de citas del paciente obtenidos.", serializer.appointmentsToJson(appointments));
    }

    public Response getDoctorAppointmentsTableData(long doctorId, boolean onlyPending) {
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
        return Response.ok("Datos de citas del doctor obtenidos.", serializer.appointmentsToJson(appointments));
    }
}
