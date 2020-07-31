package de.busam.fido2;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import de.busam.fido2.jwt.JWTAccessManager;
import de.busam.fido2.jwt.JWTContextDecoder;
import de.busam.fido2.jwt.JWTController;
import de.busam.fido2.model.user.AppRole;
import io.javalin.Javalin;
import io.javalin.core.security.SecurityUtil;
import io.javalin.plugin.json.JavalinJackson;
import io.javalin.plugin.rendering.vue.JavalinVue;
import io.javalin.plugin.rendering.vue.VueComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.Optional;

public class Main {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class.getName());

    //FIXME: The Hostname is currently used in WebAuthn standard for device registration.
    // But keep in mind the java script credentials management api can be only used without HTTPS on localhost.
    // Otherwise it will not be available if you don't use HTTPS (Client throws TypeError: navigator.credentials is undefined).
    public static final String HOSTNAME = "localhost";
    public static void main(String[] args) {
        Javalin app = Javalin.create(
                config -> {
                    config.enableWebjars();
                    config.enableDevLogging();
                    config.accessManager(new JWTAccessManager());
                }
        ).start(5000);
        addStateFunctions();
        configureJackson();
        addJWTSupport(app);
        addCommonPages(app);
        addAdminUi(app);
    }

    /**
     * This state functions are available via Vue.
     * E.g. with the state function currentUser it's possible to retrieve the currentUser in Vue
     */
    static void addStateFunctions() {
        JavalinVue.stateFunction = ctx -> Map.of("currentUser",
                Optional.ofNullable(JWTController.getUser(ctx)).orElse("anonym"));
    }

    /**
     * Configure Jackson to serialize dates in a proper format
     */
    static void configureJackson() {
        JavalinJackson.getObjectMapper().setDateFormat(new SimpleDateFormat("dd.mm.yyyy"));

        // Fail on empty beans must be deactivated because the yubico webauthn implementation uses empty
        // beans during serialization of public key credential creation options when registering device via FIDO2
        // see also https://github.com/Yubico/java-webauthn-server
        JavalinJackson.getObjectMapper().configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // to avoid serializing empty Optionals (that can't be interpreted correctly in java script client)
        // concrete problem with java-webauthn-server library (Serialization of PublicKeyCredentialCreationOptions)
        JavalinJackson.getObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_ABSENT);
        JavalinJackson.getObjectMapper().registerModule(new Jdk8Module());
    }

    static void addJWTSupport(Javalin app) {
        JWTController jwtController = new JWTController();
        app.before(JWTContextDecoder.createHeaderDecodeHandler(jwtController));
        app.before(JWTContextDecoder.createCookieDecodeHandler(jwtController));

        //there is no separate login page. Because the login is on the landing page
        app.post("/api/login", jwtController::login, SecurityUtil.roles(AppRole.ANYONE));
        app.get("/api/logout", jwtController::logout, SecurityUtil.roles(AppRole.USER, AppRole.ADMIN));
        app.get("/api/validate", jwtController::validate, SecurityUtil.roles(AppRole.USER, AppRole.ADMIN));

        addFido2Support(app);
    }

    static void addFido2Support(Javalin app){
        AuthDeviceController authDeviceController = new AuthDeviceController(HOSTNAME);
        app.post("/api/deviceregistration", authDeviceController::register,SecurityUtil.roles(AppRole.USER));

    }

    static void addCommonPages(Javalin app) {
        app.get("/", new VueComponent("<landing-page></landing-page>"),
                SecurityUtil.roles(AppRole.ANYONE));
        app.error(404, "html", new VueComponent("<not-found></not-found>"));
        app.error(401,"html", new VueComponent("<unauthorized></unauthorized>"));
    }

    static void addAdminUi(Javalin app) {
        app.get("/users", new VueComponent("<user-overview></user-overview>"),
                SecurityUtil.roles(AppRole.ADMIN));
        app.get("/users/:user-id", new VueComponent("<user-profile></user-profile>"),
                SecurityUtil.roles(AppRole.ADMIN));

        app.get("/api/users", UserController::getAll,
                SecurityUtil.roles(AppRole.ADMIN));
        app.get("/api/users/:user-id", UserController::getOne,
                SecurityUtil.roles(AppRole.ADMIN));
    }
}