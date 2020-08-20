package de.busam.fido2;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.exception.HexException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


/**
 * Data record for persisting security credentials
 * Keep in mind: both values, the userName and the userHandle identifies an user uniquely. The difference is that
 * the userHandle is explicitly used during the WebAuthn registration and authentication process and must not include
 * any personal data (name, e-mail address, or data derived from personal information). This improves data privacy.
 */
record DeviceCredential(String userName,ByteArray userHandle, PublicKeyCredentialDescriptor keyId, ByteArray publicKeyCose){
}

/**
 * At first step use an In-Memory Hash-Map to store the credentials for the users.
 */
public class DeviceCredentialsRepository implements CredentialRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceCredentialsRepository.class.getName());

    private static final Set<DeviceCredential> deviceCredentials = new HashSet<>();

    public DeviceCredentialsRepository(){
        // addTestCredentials();
    }

    /**
     * Store device credentials for a given user.
     * @param userName responds to userName in UserController
     * @param userHandle random generated id during registration process
     * @param keyId
     * @param publicKeyCose see also https://tools.ietf.org/html/rfc8152#section-7
     */
    void storeCredential(String userName, ByteArray userHandle, PublicKeyCredentialDescriptor keyId, ByteArray publicKeyCose) {
        LOGGER.info("Store Credential userName = {}, userHandle = {}, id = {}, publicKey = {}", userName, userHandle, keyId, publicKeyCose);
        deviceCredentials.add(new DeviceCredential(userName, userHandle, keyId,publicKeyCose));
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String userName) {
        return deviceCredentials.stream()
                .filter(deviceCredential -> deviceCredential.userName().equals(userName))
                .map(DeviceCredential::keyId)
                .collect(Collectors.toSet());
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
        return deviceCredentials.stream()
                .filter(deviceCredential -> deviceCredential.keyId().getId().compareTo(credentialId) == 0)
                .map( deviceCredential -> new RegisteredCredential.RegisteredCredentialBuilder.MandatoryStages()
                        .credentialId(deviceCredential.keyId().getId())
                        .userHandle(deviceCredential.userHandle())
                        .publicKeyCose(deviceCredential.publicKeyCose())
                        .build())
                .collect(Collectors.toSet());
    }

    //TODO: Set correct values for testing purpose!
    public void addTestCredentials(){
        try {
            ByteArray userHandle = ByteArray.fromHex(" ");
            ByteArray id = ByteArray.fromHex(" ");
            PublicKeyCredentialDescriptor publicKeyCredentialDescriptor = PublicKeyCredentialDescriptor.builder().id(id).build();
            ByteArray publicKeyCoase = ByteArray.fromHex(" ");
            storeCredential(" ", userHandle,publicKeyCredentialDescriptor,publicKeyCoase);
        }catch (HexException e){
            LOGGER.error("Can't add test credentials!!!");
        }
    }
}
