package bg.sofia.uni.fmi.mjt.server.login;

public class User {

    private String email;
    private String password;
    private boolean isLogged;

    public User(String email, String password, boolean isLogged) {
        this.email = email;
        this.password = password;
        this.isLogged = isLogged;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }




}
