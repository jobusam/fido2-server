package de.busam.fido2;

import com.yubico.webauthn.data.ByteArray;
import com.yubico.webauthn.data.PublicKeyCredentialDescriptor;
import com.yubico.webauthn.data.exception.HexException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceCredentialsRepositoryTest {
    private static final Logger LOGGER = LoggerFactory.getLogger(DeviceCredentialsRepositoryTest.class.getName());

    @Test
    public void test() throws HexException {
        ByteArray userHandle = ByteArray.fromHex("values from lo");
        ByteArray id = ByteArray.fromHex("values from log");
        PublicKeyCredentialDescriptor publicKeyCredentialDescriptor = PublicKeyCredentialDescriptor.builder().id(id).build();
        ByteArray publicKeyCoase = ByteArray.fromHex("values from log");
        DeviceCredential credential = new DeviceCredential("richard", userHandle,publicKeyCredentialDescriptor,publicKeyCoase);

        LOGGER.info("Store Credential userName = {}, userHandle = {}, id = {}, publicKey = {}",
                credential.userName(), credential.userHandle(), credential.keyId(), credential.publicKeyCose());
    }
}
