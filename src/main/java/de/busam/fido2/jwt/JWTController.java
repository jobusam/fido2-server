package de.busam.fido2.jwt;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.auth0.jwt.interfaces.JWTVerifier;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.busam.fido2.UserController;
import de.busam.fido2.model.user.AppRole;
import de.busam.fido2.model.user.User;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import io.javalin.http.UnauthorizedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This record represents the information body of an JWT
 */
record JWTToken(@JsonProperty("content")String content) {
}

/**
 * This record represents the information of an login post request
 */
record Credentials(@JsonProperty("username")String username,
                   @JsonProperty("password")String password) {
}

/**
 * Authentication and Authorization is provided by JWT.
 * use the implementation https://github.com/auth0/java-jwt
 */
public class JWTController {
    private static final Logger LOGGER = LoggerFactory.getLogger(JWTController.class.getName());

    // FIXME: Change that in production environment!
    private static final String JWT_SECRET = "very-special-secret";

    public static final String USER_CLAIM_ATTRIBUTE = "id";
    public static final String ROLE_CLAIM_ATTRIBUTE = "roles";
    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JWTController() {
        this.algorithm = Algorithm.HMAC256(JWT_SECRET);
        this.verifier = JWT.require(algorithm).build();
    }

    public void login(Context context) {
        Credentials c = context.bodyAsClass(Credentials.class);
        String username = c.username();
        String password = c.password();
        if (username != null && !username.isBlank()
                && password != null && !password.isBlank()) {
            User user = UserController.getUser(username);
            if (user != null) {
                //check password
                if (user.password().equals(password)) {
                    JWTToken token = new JWTToken(generateToken(user.id(), user.roles()));
                    JWTContextDecoder.addTokenToCookie(context, token.content());
                } else {
                    LOGGER.warn("Password missmatch. given pwd = {}", password);
                    throw new UnauthorizedResponse();
                }
            } else {
                LOGGER.warn("User can't be found. given user = {}", username);
                throw new UnauthorizedResponse();
            }
        } else {
            LOGGER.warn("Credentials are not valid. username = {}, password = {}", username,password);
            throw new UnauthorizedResponse();
        }
    }

    /**
     * during logout the max age of the cookie is set to 0. So the cookie will be deleted!
     */
    public void logout(Context context){
        context.cookie("jwt", "", 0);
        // redirect to landing page
        context.redirect("/");
    }

    public void validate(Context context) {
        int id = getUserId(context);
        if (UserController.getUser(id) == null) {
            LOGGER.warn("Requested User with id {} not found!", id);
            throw new NotFoundResponse();
        }
    }

    public String generateToken(int id, List<AppRole> roles) {
        String[] serializedRoles = roles.stream()
                .map(Objects::toString)
                .collect(Collectors.toList())
                .toArray(new String[roles.size()]);

        JWTCreator.Builder token = JWT.create()
                .withClaim(USER_CLAIM_ATTRIBUTE, id)
                .withArrayClaim(ROLE_CLAIM_ATTRIBUTE, serializedRoles);
        return token.sign(algorithm);
    }

    public Optional<DecodedJWT> validateToken(String token) {
        try {
            return Optional.of(verifier.verify(token));
        } catch (JWTVerificationException ex) {
            return Optional.empty();
        }
    }

    public static Integer getUserId(Context context) {
        if (JWTContextDecoder.containsJWT(context)) {
            return JWTContextDecoder.getDecodedFromContext(context)
                    .getClaim(JWTController.USER_CLAIM_ATTRIBUTE).asInt();
        }
        return null;
    }

    public static String getUser(Context context) {
        return Optional.ofNullable(getUserId(context))
                .map(UserController::getUser)
                .map(User::name)
                .orElse(null);
    }
}

