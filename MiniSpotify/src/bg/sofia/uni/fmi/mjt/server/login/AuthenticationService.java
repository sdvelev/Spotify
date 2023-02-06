package bg.sofia.uni.fmi.mjt.server.login;

import bg.sofia.uni.fmi.mjt.server.ServerReply;
import bg.sofia.uni.fmi.mjt.server.exceptions.EmailAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NotValidEmailFormatException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.security.NoSuchAlgorithmException;

import static bg.sofia.uni.fmi.mjt.server.login.SHAAlgorithm.getHash;

public class AuthenticationService {

    private final static String VALID_EMAIL_REGEX = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$";
    private final static String REGISTERED_USERS_LIST_PATH = "data" + File.separator + "authentication" +
        File.separator + "RegisteredUsersList.txt";
    private final static String INTERVAL_REGEX = " ";

    private Reader authenticationReader;

    private Writer authenticationWriter;

    public AuthenticationService() { }

    public AuthenticationService(Reader authenticationReader, Writer authenticationWriter) {
        this.authenticationReader = authenticationReader;
        this.authenticationWriter = authenticationWriter;
    }

    public synchronized User login(String email, String password) throws UserNotFoundException,
        NoSuchAlgorithmException, IODatabaseException {

        String entryToSearch = email + INTERVAL_REGEX + getHash(password);

        try (BufferedReader bufferedReader = new BufferedReader(getAppropriateReader())) {

            if (bufferedReader.lines()
                .filter(currentEntry -> currentEntry.equals(entryToSearch))
                .toList().size() == 1) {

                return new User(email, password);
            }
        } catch (IOException e) {

            throw new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        }

        throw new UserNotFoundException(ServerReply.LOGIN_COMMAND_USER_NOT_EXIST_REPLY.getReply());
    }

    public synchronized void register(String email, String password) throws
        NoSuchAlgorithmException, NotValidEmailFormatException, EmailAlreadyRegisteredException, IODatabaseException {

        if (doExist(email)) {
            throw new EmailAlreadyRegisteredException(ServerReply.REGISTER_COMMAND_ALREADY_EXIST_REPLY.getReply());
        }

        if (!email.matches(VALID_EMAIL_REGEX))     {
            throw new NotValidEmailFormatException(ServerReply.REGISTER_COMMAND_INVALID_EMAIL_REPLY.getReply());
        }

        String toWriteEntry = email + " " + getHash(password) + System.lineSeparator();

        try (BufferedWriter bufferedWriter = new BufferedWriter(getAppropriateWriter())) {

            bufferedWriter.write(toWriteEntry);
            bufferedWriter.flush();
        } catch (IOException e) {

            throw new IODatabaseException(ServerReply.IO_DATABASE_PROBLEM_REPLY.getReply(), e);
        }
    }

    public Reader getAuthenticationReader() {
        return authenticationReader;
    }

    public Writer getAuthenticationWriter() {
        return authenticationWriter;
    }

    private synchronized boolean doExist(String email) throws IODatabaseException {

        try (BufferedReader bufferedReader = new BufferedReader(getAppropriateReader())) {

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

    private synchronized Reader getAppropriateReader() throws FileNotFoundException {

        if (authenticationReader == null) {

            return new FileReader(REGISTERED_USERS_LIST_PATH);
        }

        return authenticationReader;
    }

    private synchronized Writer getAppropriateWriter() throws IOException {

        if (authenticationWriter == null) {

            return new FileWriter(REGISTERED_USERS_LIST_PATH, true);
        }

        return authenticationWriter;
    }
}
