package bg.sofia.uni.fmi.mjt.server.login;

import bg.sofia.uni.fmi.mjt.server.exceptions.EmailAlreadyRegisteredException;
import bg.sofia.uni.fmi.mjt.server.exceptions.IODatabaseException;
import bg.sofia.uni.fmi.mjt.server.exceptions.NotValidEmailFormatException;
import bg.sofia.uni.fmi.mjt.server.exceptions.UserNotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.security.NoSuchAlgorithmException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class AuthenticationServiceTest {

    private final static String REGISTERED_USERS = "sdvelev@gmail.com " +
        "8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92" + System.lineSeparator() +
        "s@s.com a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3" + System.lineSeparator();

    private final static String IO_EXCEPTION_ERROR_MESSAGE = "There is an exception in closing streams.";

    private AuthenticationService authenticationService;

    @BeforeEach
    void setTests() {

        var authenticationReader = new StringReader(REGISTERED_USERS);
        var authenticationWriter = new StringWriter();

        authenticationService = new AuthenticationService(authenticationReader, authenticationWriter);
    }

    @AfterEach
    void setTestsCleaning() throws IODatabaseException {

        try {

            authenticationService.getAuthenticationReader().close();
            authenticationService.getAuthenticationWriter().close();
        } catch(IOException e) {

            throw new IODatabaseException(IO_EXCEPTION_ERROR_MESSAGE, e);
        }
    }

    @Test
    void testRegisterSuccessfully() throws NotValidEmailFormatException, EmailAlreadyRegisteredException,
        IODatabaseException, NoSuchAlgorithmException {

        String emailToRegister = "sdvelev@outlook.com";
        String passwordToRegister = "123456";

        authenticationService.register(emailToRegister, passwordToRegister);

        String actualResult = authenticationService.getAuthenticationWriter().toString();

        String expectedResult = emailToRegister + " 8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92" +
            System.lineSeparator();

        assertEquals(expectedResult, actualResult, "Actual result entry after registration is not the same as " +
            "the expected.");
    }

    @Test
    void testRegisterEmailAlreadyRegisteredException() {

        String emailToRegister = "sdvelev@gmail.com";
        String passwordToRegister = "123456";

        assertThrows(EmailAlreadyRegisteredException.class, () -> authenticationService
            .register(emailToRegister, passwordToRegister),
            "EmailAlreadyRegisteredException is expected but not thrown.");
    }

    @Test
    void testRegisterEmailNotValidEmailFormatException() {

        String emailToRegister = "sdvelev@outlook";
        String passwordToRegister = "123456";

        assertThrows(NotValidEmailFormatException.class, () -> authenticationService
                .register(emailToRegister, passwordToRegister),
            "NotValidEmailFormatException is expected but not thrown.");
    }

    @Test
    void testLoginSuccessfully() throws UserNotFoundException, IODatabaseException, NoSuchAlgorithmException {

        String emailToLogin = "sdvelev@gmail.com";
        String passwordToLogin = "123456";

        User expectedUser = new User("sdvelev@gmail.com", "123456");

        assertEquals(expectedUser, authenticationService.login(emailToLogin, passwordToLogin),
            "Actual returned user is not the same as the expected.");
    }

    @Test
    void testLoginUserNotFoundException() {

        String emailToLogin = "sdvelev@outlook.com";
        String passwordToLogin = "123456";

        assertThrows(UserNotFoundException.class, () -> authenticationService
                .login(emailToLogin, passwordToLogin),
            "UserNotFoundException is expected but not thrown.");
    }
}
