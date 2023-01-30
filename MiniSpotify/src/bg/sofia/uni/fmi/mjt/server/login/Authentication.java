package bg.sofia.uni.fmi.mjt.server.login;

import bg.sofia.uni.fmi.mjt.server.ServerReply;
import bg.sofia.uni.fmi.mjt.server.exceptions.EmailAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NotValidEmailFormatException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import static bg.sofia.uni.fmi.mjt.server.login.SHAAlgorithm.getHash;

public class Authentication {

    private final static String VALID_EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";

    private final static String REGISTERED_USERS_LIST_PATH = "data" + File.separator + "RegisteredUsersList.txt";

    private final static String INTERVAL_REGEX = " ";


    public static User login(String email, String password) throws UserNotFoundException,
        NoSuchAlgorithmException, IODatabaseException {

        String entryToSearch = email + INTERVAL_REGEX + getHash(password);
        try (BufferedReader bufferedReader = new BufferedReader(new
            FileReader(REGISTERED_USERS_LIST_PATH))) {

            if (bufferedReader.lines()
                .filter(currentEntry -> currentEntry.equals(entryToSearch))
                .toList().size() == 1) {

                return new User(email, password, true);
            }

        } catch (IOException e) {
            throw new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        }

        throw new UserNotFoundException(ServerReply.LOGIN_COMMAND_USER_NOT_EXIST_REPLY.getReply());
    }

    private static boolean doExist(String email) throws IODatabaseException {

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(REGISTERED_USERS_LIST_PATH))) {

            if (bufferedReader.lines()
                .filter(currentEntry -> currentEntry.split(INTERVAL_REGEX)[0].equals(email))
                .toList().size() == 1) {
                return true;
            }
        } catch (IOException e) {

            throw new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply());
        }

        return false;
    }

    public static void register(String email, String password) throws
        NoSuchAlgorithmException, NotValidEmailFormatException, EmailAlreadyRegisteredException, IODatabaseException {

        if (doExist(email)) {
            throw new EmailAlreadyRegisteredException(ServerReply.REGISTER_COMMAND_ALREADY_EXIST_REPLY.getReply());
        }

        if (!email.matches(VALID_EMAIL_REGEX))     {
            throw new NotValidEmailFormatException(ServerReply.REGISTER_COMMAND_INVALID_EMAIL_REPLY.getReply());
        }

        String toWriteEntry = email + " " + getHash(password) + System.lineSeparator();
        try (BufferedWriter bufferedWriter = new BufferedWriter(new
            FileWriter(REGISTERED_USERS_LIST_PATH, true))) {

            bufferedWriter.write(toWriteEntry);
            bufferedWriter.flush();
        } catch (IOException e) {
            throw new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        }
    }

    public static void main(String[] args)
        throws NotValidEmailFormatException, NoSuchAlgorithmException, UserNotFoundException,
        EmailAlreadyRegisteredException, IODatabaseException {

        String email = "sampleEmail2@abv.bg";
        String password = "123456";
        register(email, password);

       // User res = login(email, password);
      //  System.out.println(res.getEmail());
    }

}
