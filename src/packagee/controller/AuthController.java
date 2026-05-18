package packagee.controller;

import packagee.Administrator;
import packagee.Doctor;
import packagee.Patient;
import packagee.User;
import packagee.controller.response.Response;
import packagee.dto.JsonSerializer;
import packagee.storage.DataStore;

public class AuthController {

    private final DataStore store;
    private final JsonSerializer serializer;

    public AuthController() {
        this(DataStore.getInstance());
    }

    public AuthController(DataStore store) {
        this.store = store;
        this.serializer = new JsonSerializer();
    }

    public Response login(String username, String password) {
        User user = store.findUserByUsername(username);
        if (user == null || password == null || !user.getPassword().equals(password)) {
            return Response.unauthorized("Usuario o contrasena incorrectos.");
        }

        return Response.ok("Inicio de sesion correcto.", serializer.userToJson(user, roleOf(user)));
    }

    private String roleOf(User user) {
        if (user instanceof Administrator) {
            return "ADMIN";
        }
        if (user instanceof Patient) {
            return "PATIENT";
        }
        if (user instanceof Doctor) {
            return "DOCTOR";
        }
        return "USER";
    }
}
