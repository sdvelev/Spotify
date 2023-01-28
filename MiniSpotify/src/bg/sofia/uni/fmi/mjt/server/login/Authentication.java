package bg.sofia.uni.fmi.mjt.server.login;

import bg.sofia.uni.fmi.mjt.server.exceptions.EmailAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NotValidEmailFormatException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import static bg.sofia.uni.fmi.mjt.server.login.SHAAlgorithm.getHash;

public class Authentication {

    private final static String VALID_EMAIL_REGEX = "^[a-zA-Z0-9+_.-]+@[a-zA-Z0-9.-]+$";

    public static synchronized User login(String email, String password) throws UserNotFoundException, NoSuchAlgorithmException {

        String entryToSearch = email + " " + getHash(password);

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader("data/registeredUsersList.txt"))) {

            if(bufferedReader.lines()
                .filter(currentEntry -> currentEntry.equals(entryToSearch))
                .toList().size() == 1) {

                return new User(email, password, true);
            }

        } catch (IOException e) {

            System.out.println("");
        }

        throw new UserNotFoundException("User with such an email and password does not exist");
    }

    private final static String BEFORE_EMAIL = "^";
    private final static String AFTER_EMAIL = " [.]+$";
    private final static String INTERVAL_REGEX = " ";

    private static synchronized boolean doesExist(String email) {

        try(BufferedReader bufferedReader = new BufferedReader(new FileReader("data/registeredUsersList.txt"))) {

            if(bufferedReader.lines()
                .filter(currentEntry -> currentEntry.split(INTERVAL_REGEX)[0].equals(email))
                .toList().size() == 1) {

                return true;
            }

        } catch (IOException e) {

            System.out.println("");
        }

        return false;
    }

    public static synchronized boolean register(String email, String password) throws
        NoSuchAlgorithmException, NotValidEmailFormatException, EmailAlreadyRegisteredException {

        if (doesExist(email)) {

            throw new EmailAlreadyRegisteredException("There is already a profile with that email. Please, try to login.");
        }

        if (!email.matches(VALID_EMAIL_REGEX))     {
            throw new NotValidEmailFormatException("The provided email is not valid");
        }

        String toWrite = email + " " + getHash(password) + System.lineSeparator();

        try(BufferedWriter bufferedWriter = new BufferedWriter(new
            FileWriter("data/registeredUsersList.txt", true))) {

            bufferedWriter.write(toWrite);
            bufferedWriter.flush();
        } catch (IOException e) {

            System.out.println("There is a problem writing to database. Please, try again later");
            return false;
        }

        return true;
    }

    public static void main(String[] args)
        throws NotValidEmailFormatException, NoSuchAlgorithmException, UserNotFoundException,
        EmailAlreadyRegisteredException {

        String email = "sampleEmail2@abv.bg";
        String password = "123456";
        register(email, password);

       // User res = login(email, password);
      //  System.out.println(res.getEmail());
    }

}
