package packagee.storage;

import packagee.Appointment;
import packagee.Hospitalization;

public class IdGenerator {

    public String nextAppointmentId(long patientId, DataStore store) {
        String prefix = "A-" + patientId + "-";
        int nextNumber = findNextAppointmentNumber(prefix, store);
        return prefix + String.format("%04d", nextNumber);
    }

    public String nextHospitalizationId(long patientId, DataStore store) {
        String prefix = "H-" + patientId + "-";
        int nextNumber = findNextHospitalizationNumber(prefix, store);
        return prefix + String.format("%04d", nextNumber);
    }

    private int findNextAppointmentNumber(String prefix, DataStore store) {
        int max = -1;
        for (Appointment appointment : store.getAppointments()) {
            if (appointment.getId().startsWith(prefix)) {
                int value = parseSuffix(appointment.getId(), prefix);
                if (value > max) {
                    max = value;
                }
            }
        }
        return max + 1;
    }

    private int findNextHospitalizationNumber(String prefix, DataStore store) {
        int max = -1;
        for (Hospitalization hospitalization : store.getHospitalizations()) {
            if (hospitalization.getId().startsWith(prefix)) {
                int value = parseSuffix(hospitalization.getId(), prefix);
                if (value > max) {
                    max = value;
                }
            }
        }
        return max + 1;
    }

    private int parseSuffix(String id, String prefix) {
        try {
            return Integer.parseInt(id.substring(prefix.length()));
        } catch (NumberFormatException ex) {
            return -1;
        }
    }
}
