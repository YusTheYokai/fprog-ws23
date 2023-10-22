package at.technikum;

import java.util.Arrays;
import java.util.stream.Collectors;

public class LogLeveledException extends Exception {

    private final LogLevel logLevel;
    private final String[] messages;

    public LogLeveledException(LogLevel logLevel, String... messages) {
        super(Arrays.stream(messages).collect(Collectors.joining()));
        this.logLevel = logLevel;
        this.messages = messages;
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }

    public String[] getMessages() {
        return messages;
    }
}
