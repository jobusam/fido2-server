package de.busam.fido2;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yubico.webauthn.*;
import com.yubico.webauthn.data.*;
import com.yubico.webauthn.exception.AssertionFailedException;
import com.yubico.webauthn.exception.RegistrationFailedException;
import de.busam.fido2.jwt.JWTContextDecoder;
import de.busam.fido2.jwt.JWTController;
import de.busam.fido2.model.user.AppRole;
import de.busam.fido2.model.user.User;
import io.javalin.http.BadRequestResponse;
import io.javalin.http.Context;
import io.javalin.http.UnauthorizedResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiFunction;


public class AuthDeviceController {
    private static final Logger LOGGER = LoggerFactory.getLogger(AuthDeviceController.class.getName());

    private final RelyingParty rp;
    private final DeviceCredentialsRepository deviceCredentialsRepository;
    private final BiFunction<Integer, List<AppRole>, String> generateToken;

    // FIXME: this is a stupid workaround!
    private PublicKeyCredentialCreationOptions request;
    private AssertionRequest assertionRequest;

    /**
     * This record represents the information of an login post request
     */
    record UserAuthRequest(@JsonProperty("username")String username ){
    }

    public AuthDeviceController(String hostname, int port, BiFunction<Integer, List<AppRole>, String> generateToken) {
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
        this.generateToken = generateToken;
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

    public void startRegistration(Context context) {
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
                    getUser(context).name(), request.getUser().getId(), result.getKeyId(), result.getPublicKeyCose());

        } catch (RegistrationFailedException | IOException e) {
            LOGGER.error("Can't parse Device Registation Response {}", responseJson, e);
            throw new BadRequestResponse();
        }
    }

    public void startAuthentication(Context context){
        UserAuthRequest userAuthRequest = context.bodyAsClass(UserAuthRequest.class);
        if(userAuthRequest.username == null || userAuthRequest.username.isEmpty()){
            LOGGER.error("Missing username to start authentication");
            throw new BadRequestResponse();
        }
        assertionRequest = rp.startAssertion(StartAssertionOptions.builder()
                .username(Optional.of(userAuthRequest.username))
                .build());
        context.json(assertionRequest);
    }

    public void finishAuthentication(Context context){
        LOGGER.info("finishAuthentication called");
        String responseJson = context.body();
        try {
            PublicKeyCredential<AuthenticatorAssertionResponse, ClientAssertionExtensionOutputs> pkc =
                PublicKeyCredential.parseAssertionResponseJson(responseJson);

            AssertionResult result = rp.finishAssertion(FinishAssertionOptions.builder()
                    .request(assertionRequest)
                    .response(pkc)
                    .build());

            if (result.isSuccess()) {
                LOGGER.info("User {} successfully authenticated via FIDO2 device!!!!",result.getUsername());
                User user = UserController.getUser(result.getUsername());
                JWTContextDecoder.addTokenToCookie(context, this.generateToken.apply(user.id(), user.roles()));
            }
            else {
                LOGGER.warn("Can't authenticate user");
                throw new UnauthorizedResponse();
            }
        } catch (AssertionFailedException | IOException e) {
            LOGGER.error("Can't authenticate user = {}", e);
            throw new UnauthorizedResponse();
        }
    }

}
