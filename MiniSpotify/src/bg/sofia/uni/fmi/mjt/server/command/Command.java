package bg.sofia.uni.fmi.mjt.server.command;

import java.util.List;

public record Command(String command, List<String> arguments) { }
