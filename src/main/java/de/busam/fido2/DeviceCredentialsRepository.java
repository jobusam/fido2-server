package de.busam.fido2;

import com.yubico.webauthn.CredentialRepository;
import com.yubico.webauthn.RegisteredCredential;
import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Optional;
import java.util.Set;

public class DeviceCredentialsRepository implements CredentialRepository {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceCredentialsRepository.class.getName());

    // FIXME: implement following code

    void storeCredential(String username, PublicKeyCredentialDescriptor keyId, ByteArray publicKeyCose) {
        LOGGER.info("Store Credential username = {}, id = {}, publicKey = {}", username, keyId, publicKeyCose);
        LOGGER.error("Not Implemented: storeCredential");
    }

    @Override
    public Set<PublicKeyCredentialDescriptor> getCredentialIdsForUsername(String username) {
        LOGGER.error("Not Implemented: getCredentialIdsForUsername with username = {}",username);
        return null;
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
        //throw new RuntimeException("Not yet implemented");
        LOGGER.error("Not Implemented: lookupAll with credentialId = {}",credentialId);
        return Collections.emptySet();
    }
}
