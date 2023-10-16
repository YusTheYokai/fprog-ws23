package at.technikum;

import java.io.PrintStream;
import java.time.LocalDateTime;

public class Logger {

    public static void info(String message) {
        log(Level.INFO, message);
    }

    public static void error(String message) {
        log(Level.ERROR, message);
    }

    private static void log(Level level, String message) {
        var log = String.format("%s %s %s", LocalDateTime.now().toString(), level.name(), message);
        level.getStream().println(log);
    }

    private enum Level {

        INFO(System.out),
        ERROR(System.err);

        private PrintStream stream;

        Level(PrintStream stream) {
            this.stream = stream;
        }

        public PrintStream getStream() {
            return stream;
        }
    }
}
