package bg.sofia.uni.fmi.mjt.server.command;

import java.util.List;

public record Command(String command, List<String> arguments) {


    /*public static void main(String[] args) {
        Command a = new Command("fdsfsd", List.of("1", "2"));
        System.out.println(a.command());
        System.out.println(a.arguments().get(0));
    }*/
}
