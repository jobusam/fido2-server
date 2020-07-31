package de.busam.fido2;

import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialCreationOptions;
import com.yubico.webauthn.data.RelyingPartyIdentity;
import com.yubico.webauthn.data.UserIdentity;
import de.busam.fido2.jwt.JWTController;
import de.busam.fido2.model.user.User;
import io.javalin.http.Context;
import io.javalin.http.NotFoundResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.Charset;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.IntStream;

public class AuthDeviceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthDeviceController.class.getName());

    private final RelyingParty rp;

    public AuthDeviceController(String hostname){
        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(hostname)
                .name("Fido 2 Test Application")
                .build();
        rp = RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(new UserController())
                .build();
    }

    public void register(Context context) {
        Optional<User> user = Optional.ofNullable(JWTController.getUserId(context))
                .map(UserController::getUser);
        if (user.isEmpty()){
            LOGGER.error("Can't retrieve metadata from current user!!!");
        throw new NotFoundResponse();
        }

        String username = user.get().name();
        String email = user.get().email();
        String userHandle = UUID.randomUUID().toString();
        LOGGER.info("userHandle = {}",userHandle);
        PublicKeyCredentialCreationOptions request = rp.startRegistration(StartRegistrationOptions.builder()
                .user(UserIdentity.builder()
                        .name(username)
                        .displayName(email)
                        .id(new ByteArray(userHandle.getBytes()))
                        .build())
                .build());
        LOGGER.info("challenge = {}",request.getChallenge());
        LOGGER.info("challenge2 = {}",new String(request.getChallenge().getBytes()));
        context.json(request);
    }
}
