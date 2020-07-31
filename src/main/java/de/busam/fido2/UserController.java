package de.busam.fido2;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import de.busam.fido2.model.user.AppRole;
import de.busam.fido2.model.user.User;
import de.busam.fido2.model.user.UserDetails;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * This controller holds the user data
 */
public class UserController implements CredentialRepository {
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

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        LOGGER.error("Not Implemented: getCredentialIdsForUsername with username = {}",username);
        return null;
    }

    @Override
    public Optional<ByteArray> getUserHandleForUsername(String username) {
        throw new RuntimeException("Not yet implemented");
        //return Optional.empty();
    }

    @Override
    public Optional<String> getUsernameForUserHandle(ByteArray userHandle) {
        throw new RuntimeException("Not yet implemented");
        //return Optional.empty();
    }

    @Override
    public Optional<RegisteredCredential> lookup(ByteArray credentialId, ByteArray userHandle) {
        throw new RuntimeException("Not yet implemented");
        //return Optional.empty();
    }

    @Override
    public Set<RegisteredCredential> lookupAll(ByteArray credentialId) {
        throw new RuntimeException("Not yet implemented");
        //return null;
    }
}
