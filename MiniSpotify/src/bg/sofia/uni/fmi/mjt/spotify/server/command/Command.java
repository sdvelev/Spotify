package bg.sofia.uni.fmi.mjt.spotify.server.command;

import java.util.List;

public record Command(String command, List<String> arguments) { }
