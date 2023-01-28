package bg.sofia.uni.fmi.mjt.server;

import bg.sofia.uni.fmi.mjt.server.login.User;

public class StreamingPlatform {

    private User user;
    private boolean isLogged;

    public void setUser(User user) {
        this.user = user;
    }

    public void setIsLogged(boolean isLogged) {
        this.isLogged = isLogged;
    }

}
