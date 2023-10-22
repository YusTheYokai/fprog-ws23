package at.technikum;

import java.io.PrintStream;

public enum LogLevel {

    INFO(System.out),
    ERROR(System.err),
    FATAL(System.err);

    private PrintStream stream;

    LogLevel(PrintStream stream) {
        this.stream = stream;
    }

    public PrintStream getStream() {
        return stream;
    }
}
