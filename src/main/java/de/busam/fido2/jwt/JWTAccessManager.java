package de.busam.fido2.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import de.busam.fido2.model.user.AppRole;
import io.javalin.core.security.AccessManager;
import io.javalin.core.security.Role;
import io.javalin.http.Context;
import io.javalin.http.Handler;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class JWTAccessManager implements AccessManager {
    private Map<String, Role> rolesMapping;

    private List<AppRole> extractRoles(Context context) {
        if (!JWTContextDecoder.containsJWT(context)) {
            return Collections.emptyList();
        }
        DecodedJWT jwt = JWTContextDecoder.getDecodedFromContext(context);
        return jwt.getClaim(JWTController.ROLE_CLAIM_ATTRIBUTE).asList(AppRole.class);
    }

    @Override
    public void manage(Handler handler, Context context, Set<Role> permittedRoles) throws Exception {
        List<AppRole> roles = extractRoles(context);

        if (permittedRoles.contains(AppRole.ANYONE)) {
            handler.handle(context);
        } else if (permittedRoles.contains(AppRole.ADMIN) && roles.contains(AppRole.ADMIN)) {
            handler.handle(context);
        } else if (permittedRoles.contains(AppRole.USER) && roles.contains(AppRole.USER)) {
            //FIXME: check if url only belongs to it's user data. Currently any loged in
            //user can see all other user data!
            handler.handle(context);
        } else {
            context.status(401).result("Unauthorized");
        }

    }
}
