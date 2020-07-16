package de.busam.fido2.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import io.javalin.http.InternalServerErrorResponse;

import java.util.Optional;

/**
 * This implementation is responsible to extract the JWT either
 * from HTTP Authorization Header or Cookie and set it a separate
 * Entry in the HTTP Context.
 */
public class JWTContextDecoder {

    private final static String CONTEXT_ATTRIBUTE = "jwt";
    private final static String COOKIE_KEY = "jwt";


    public static boolean containsJWT(Context context) {
        return context.attribute(CONTEXT_ATTRIBUTE) != null;
    }

    public static Context addDecodedToContext(Context context, DecodedJWT jwt) {
        context.attribute(CONTEXT_ATTRIBUTE, jwt);
        return context;
    }

    public static DecodedJWT getDecodedFromContext(Context context) {
        Object attribute = context.attribute(CONTEXT_ATTRIBUTE);

        if (!(attribute instanceof DecodedJWT)) {
            throw new InternalServerErrorResponse("The context carried invalid object as JavalinJWT");
        }

        return (DecodedJWT) attribute;
    }

    public static Optional<String> getTokenFromHeader(Context context) {
        return Optional.ofNullable(context.header("Authorization"))
                .flatMap(header -> {
                    String[] split = header.split(" ");
                    if (split.length != 2 || !split[0].equals("Bearer")) {
                        return Optional.empty();
                    }

                    return Optional.of(split[1]);
                });
    }

    public static Optional<String> getTokenFromCookie(Context context) {
        return Optional.ofNullable(context.cookie(COOKIE_KEY));
    }

    public static Context addTokenToCookie(Context context, String token) {
        return context.cookie(COOKIE_KEY, token);
    }

    public static Handler createHeaderDecodeHandler(JWTController jwtController) {
        return context -> getTokenFromHeader(context)
                .flatMap(jwtController::validateToken)
                .ifPresent(jwt -> JWTContextDecoder.addDecodedToContext(context, jwt));
    }

    public static Handler createCookieDecodeHandler(JWTController jwtController) {
        return context -> getTokenFromCookie(context)
                .flatMap(jwtController::validateToken)
                .ifPresent(jwt -> JWTContextDecoder.addDecodedToContext(context, jwt));
    }
}

