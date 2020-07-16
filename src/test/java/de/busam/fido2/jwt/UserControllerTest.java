package de.busam.fido2.jwt;

import com.auth0.jwt.interfaces.DecodedJWT;
import de.busam.fido2.model.user.AppRole;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class UserControllerTest {

    @Test
    public void testGenerateAndValidateToken() {
        JWTController testObject = new JWTController();

        String jwtToken = testObject.generateToken(12,
                List.of(AppRole.USER, AppRole.ADMIN));
        assertNotNull(jwtToken);

        Optional<DecodedJWT> decodedJWT = testObject.validateToken(jwtToken);
        assertTrue(decodedJWT.isPresent());
    }
}
