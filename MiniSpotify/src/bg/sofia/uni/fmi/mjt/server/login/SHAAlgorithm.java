package bg.sofia.uni.fmi.mjt.server.login;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHAAlgorithm {

    private static byte[] getSHA(String toEncrypt) throws NoSuchAlgorithmException {

        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        return messageDigest.digest(toEncrypt.getBytes(StandardCharsets.UTF_8));

    }

    private static String toHexadecimal(byte[] hash) {

        BigInteger bigInteger = new BigInteger(1, hash);
        StringBuilder hexadecimalString = new StringBuilder(bigInteger.toString(16));

        while(hexadecimalString.length() < 32) {

            hexadecimalString.insert(0, '0');
        }

        return hexadecimalString.toString();
    }

    public static String getHash(String toEncrypt) throws NoSuchAlgorithmException {

        return toHexadecimal(getSHA(toEncrypt));
    }

    public static void main(String[] args) {

        try {

            String str = "JavaTpoint";
            String hash = getHash(str);
            System.out.println("\n" + str + " : " + hash);
        } catch(NoSuchAlgorithmException e) {

            System.out.println("There is a mistake in algorithm");
        }
    }
}
