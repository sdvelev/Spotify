package bg.sofia.uni.fmi.mjt.server.login;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHAAlgorithm {

    private final static int RADIX = 16;
    private final static int MAX_LENGTH = 32;
    private final static String ALGORITHM_NAME = "SHA-256";

    private static byte[] getSHA(String toEncrypt) throws NoSuchAlgorithmException {

        MessageDigest messageDigest = MessageDigest.getInstance(ALGORITHM_NAME);
        return messageDigest.digest(toEncrypt.getBytes(StandardCharsets.UTF_8));
    }

    private static String toHexadecimal(byte[] hash) {

        BigInteger bigInteger = new BigInteger(1, hash);
        StringBuilder hexadecimalString = new StringBuilder(bigInteger.toString(RADIX));

        while (hexadecimalString.length() < MAX_LENGTH) {

            hexadecimalString.insert(0, '0');
        }

        return hexadecimalString.toString();
    }

    public static String getHash(String toEncrypt) throws NoSuchAlgorithmException {

        return toHexadecimal(getSHA(toEncrypt));
    }
}
