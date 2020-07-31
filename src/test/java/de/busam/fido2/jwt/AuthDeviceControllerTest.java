package de.busam.fido2.jwt;

import de.busam.fido2.AuthDeviceController;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class AuthDeviceControllerTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthDeviceControllerTest.class.getName());

    @Test
    public void testSerialization(){
        String encodedChallenge = "sr_sEH4-E_IpIpPa_MsrQfzshM8uETxEbml-CVkxzV0";
        byte[] result = Base64.getDecoder().decode(encodedChallenge);
        LOGGER.info("Result = {}",result);
    }
}
