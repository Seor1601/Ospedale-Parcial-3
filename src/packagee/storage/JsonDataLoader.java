package packagee.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import org.json.JSONArray;
import org.json.JSONObject;
import packagee.Administrator;
import packagee.Doctor;
import packagee.Patient;
import packagee.Specialty;

public class JsonDataLoader {

    public void loadInitialData(DataStore store) {
        Path usersPath = Path.of("json", "users.json");
        if (!Files.exists(usersPath)) {
            return;
        }

        try {
            String content = Files.readString(usersPath);
            JSONObject root = new JSONObject(content);
            JSONArray users = root.getJSONArray("users");

            for (int i = 0; i < users.length(); i++) {
                JSONObject user = users.getJSONObject(i);
                String type = user.getString("type");

                if ("admin".equals(type)) {
                    store.addAdministrator(new Administrator(
                            user.getLong("id"),
                            user.getString("username"),
                            user.getString("firstname"),
                            user.getString("lastname"),
                            user.getString("password")
                    ));
                } else if ("patient".equals(type)) {
                    store.addPatient(new Patient(
                            user.getLong("id"),
                            user.getString("username"),
                            user.getString("firstname"),
                            user.getString("lastname"),
                            user.getString("password"),
                            user.getString("email"),
                            LocalDate.parse(user.getString("birthdate")),
                            user.getBoolean("gender"),
                            user.getLong("phone"),
                            user.getString("address")
                    ));
                } else if ("doctor".equals(type)) {
                    store.addDoctor(new Doctor(
                            user.getLong("id"),
                            user.getString("username"),
                            user.getString("firstname"),
                            user.getString("lastname"),
                            user.getString("password"),
                            parseSpecialty(user.getString("specialty")),
                            user.getString("licenceNumber"),
                            user.getString("assignedOffice")
                    ));
                }
            }
        } catch (IOException ex) {
            throw new IllegalStateException("No se pudo leer json/users.json.", ex);
        }
    }

    private Specialty parseSpecialty(String value) {
        if ("ORTHOPEDICS".equals(value)) {
            return Specialty.TRAUMATOLOGY_ORTHOPEDICS;
        }
        if ("GYNECOLOGY".equals(value)) {
            return Specialty.GYNECOLOGY_OBSTETRICS;
        }
        return Specialty.valueOf(value);
    }
}
