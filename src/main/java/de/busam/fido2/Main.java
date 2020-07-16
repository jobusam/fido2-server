package de.busam.fido2;

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

    public static void main(String[] args) {
        Javalin app = Javalin.create(
                config -> {
                    config.enableWebjars();
                    config.enableDevLogging();
                    config.accessManager(new JWTAccessManager());
                    // default request cache size is 4096 Bytes
                    //config.requestCacheSize = 1024L * 1024 * 1024;
                }
        ).start(5000);
        addStateFunctions();
        configureJackson();
        addJWTSupport(app);
        addCommonPages(app);
        addAdminUi(app);

        // Don't use this in production environment
        //addFileUpload(app);
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
    }

    static void addJWTSupport(Javalin app) {
        JWTController jwtController = new JWTController();
        app.before(JWTContextDecoder.createHeaderDecodeHandler(jwtController));
        app.before(JWTContextDecoder.createCookieDecodeHandler(jwtController));

        //there is no separate login page. Because the login is on the landing page
        app.post("/login", jwtController::login, SecurityUtil.roles(AppRole.ANYONE));
        app.get("/logout", jwtController::logout, SecurityUtil.roles(AppRole.USER, AppRole.ADMIN));
        app.get("/api/validate", jwtController::validate, SecurityUtil.roles(AppRole.USER, AppRole.ADMIN));
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