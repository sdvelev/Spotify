package bg.sofia.uni.fmi.mjt.spotify.server.login;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.security.NoSuchAlgorithmException;

import static bg.sofia.uni.fmi.mjt.spotify.server.login.SHAAlgorithm.getHash;

public class SHAlgorithmTest {

    @Test
    void testGetHashOfWords() {
        try {
            String toEncrypt = "Strong Password";
            String expectedResult = "f96c34c56b8847e381f0eb2f0efad8b354bd641c89af9349106b8bbb17c12eea";

            String actualResult = getHash(toEncrypt);

            Assertions.assertEquals(expectedResult, actualResult,
                "The actual result of SHA-256 algorithm is not the same as the expected.");
        } catch (NoSuchAlgorithmException e) {
            Assertions.fail("There is a problem in hashing algorithm");
        }
    }

}
