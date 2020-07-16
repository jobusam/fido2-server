package de.busam.fido2;

import de.busam.fido2.model.user.AppRole;
import de.busam.fido2.model.user.User;
import de.busam.fido2.model.user.UserDetails;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This controller holds the user data
 */
public class UserController {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserController.class.getName());

    static Map<Integer, User> users = new HashMap<>();

    static {
        users.put(1, new User(1, "richard", "richard@email-domain.eu",
                "test123", List.of(AppRole.USER, AppRole.ADMIN),
                new UserDetails("01.01.2001", "Somewhere")));
        users.put(2, new User(2, "madlene", "madlene-fellow@email-domain.eu",
                "test123", List.of(AppRole.USER), null));
    }

    static void getAll(Context context) {
        context.json(users.values());
    }

    static void getOne(Context context) {
        var id = Integer.valueOf(context.pathParam("user-id"));
        if (users.containsKey(id)) {
            context.json(users.get(id));
        } else {
            LOGGER.warn("Requested User with id {} not found!", id);
            throw new NotFoundResponse();
        }
    }

    public static User getUser(int id) {
        if (users.containsKey(id)) {
            return users.get(id);
        } else {
            LOGGER.warn("Requested User with id {} not found!", id);
            return null;
        }
    }

    public static User getUser(String username) {
        return users.values().stream()
                .filter(user -> user.name().equals(username))
                .findFirst().orElse(null);
    }
}
