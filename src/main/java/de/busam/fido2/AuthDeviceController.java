package de.busam.fido2;

import com.yubico.webauthn.FinishRegistrationOptions;
import com.yubico.webauthn.RegistrationResult;
import com.yubico.webauthn.RelyingParty;
import com.yubico.webauthn.StartRegistrationOptions;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.RegistrationFailedException;
import de.busam.fido2.jwt.JWTController;
import de.busam.fido2.model.user.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class AuthDeviceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthDeviceController.class.getName());

    private final RelyingParty rp;
    private final DeviceCredentialsRepository deviceCredentialsRepository;

    // FIXME: this is a stupid workaround!
    private PublicKeyCredentialCreationOptions request;

    public AuthDeviceController(String hostname, int port) {
        RelyingPartyIdentity rpIdentity = RelyingPartyIdentity.builder()
                .id(hostname)
                .name("Fido 2 Test Application")
                .build();
        deviceCredentialsRepository = new DeviceCredentialsRepository();
        rp = RelyingParty.builder()
                .identity(rpIdentity)
                .credentialRepository(deviceCredentialsRepository)
                // set origin with protocol (https), hostname and port to verify PublicKeyCredentials from client!
                .origins(Set.of(String.format("https://%s:%d", hostname, port)))
                // TODO: requesting attestations in future
                //.attestationConveyancePreference(AttestationConveyancePreference.DIRECT)
                .build();
    }

    public User getUser(Context context) {
        Optional<User> user = Optional.ofNullable(JWTController.getUserId(context))
                .map(UserController::getUser);
        if (user.isEmpty()) {
            LOGGER.error("Can't retrieve metadata from current user!!!");
            throw new UnauthorizedResponse();
        }
        return user.get();
    }

    public void initRegistration(Context context) {
        var user = getUser(context);
        String username = user.name();
        String email = user.email();
        String userHandle = UUID.randomUUID().toString();
        LOGGER.info("Create PKI Create Options for username {} with userHandle = {}", username, userHandle);
        request = rp.startRegistration(StartRegistrationOptions.builder()
                .user(UserIdentity.builder()
                        .name(username)
                        .displayName(email)
                        .id(new ByteArray(userHandle.getBytes()))
                        .build())
                // Use Crypto-Stick e.g. Nitrokey or Yubikey to register with FIDO2
                .authenticatorSelection(
                        AuthenticatorSelectionCriteria.builder()
                                .authenticatorAttachment(AuthenticatorAttachment.CROSS_PLATFORM)
                                .build())
                .build());
        context.json(request);
    }

    public void finishRegistration(Context context) {
        LOGGER.info("finishRegistration called");
        String responseJson = context.body();
        try {
            PublicKeyCredential<AuthenticatorAttestationResponse, ClientRegistrationExtensionOutputs> pkc =
                    PublicKeyCredential.parseRegistrationResponseJson(responseJson);
            RegistrationResult result = rp.finishRegistration(FinishRegistrationOptions.builder()
                    .request(request)
                    .response(pkc)
                    .build());

            // if attestation is required by server than you have to check if the attestation is valid at this point
            deviceCredentialsRepository.storeCredential(
                    getUser(context).name(), result.getKeyId(), result.getPublicKeyCose());

        } catch (RegistrationFailedException | IOException e) {
            LOGGER.error("Can't parse Device Registation Response {}", responseJson, e);
            throw new BadRequestResponse();
        }
    }

}
