package at.technikum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {

    private static final String BEGIN_END_INDICATOR = "\\*\\*\\* .* \\*\\*\\*";
    private static final String CHAPTER_DELMITER = "CHAPTER";

    private static final Function<LogLevel, Function<String, LogLeveledException>> createLogLeveledException = logLevel -> message -> new LogLeveledException(logLevel, message);
    private static final Function<String, LogLeveledException> error = createLogLeveledException.apply(LogLevel.ERROR);
    private static final Function<String, LogLeveledException> fatal = createLogLeveledException.apply(LogLevel.FATAL);

    private static final Function<String, Optional<Stream<String>>> readFile = name -> {
        try {
            return Optional.of(Files.lines(Paths.get(name)));
        } catch (IOException e) {
            return Optional.empty();
        }
    };

    private static final Supplier<Optional<Stream<String>>> readWarTerms = () -> readFile.apply("src/main/resources/war_terms.txt");
    private static final Supplier<Optional<Stream<String>>> readPeaceTerms = () -> readFile.apply("src/main/resources/peace_terms.txt");

    public static final void main(String[] args) {
        try {
            pureMain(args);
        } catch (LogLeveledException e) {
            Arrays.stream(e.getMessages())
                    .forEachOrdered(message -> log(e.getLogLevel(), message));
        }
    }

    private static void pureMain(String[] args) throws LogLeveledException {
        if (args.length != 1) {
            throw new LogLeveledException(LogLevel.ERROR, "Invalid number of arguments.", "Usage: java -jar <jar-file> <text-file>");
        }

        List<String> warTerms = readWarTerms.get()
                .map(App::clean)
                .orElseThrow(() -> fatal.apply("Could not read war terms."))
                .toList();

        List<String> peaceTerms = readPeaceTerms.get()
                .map(App::clean)
                .orElseThrow(() -> fatal.apply("Could not read peace terms."))
                .toList();

        Stream<String> bookLines = readFile.apply(args[0]).orElseThrow(() -> error.apply(String.format("Could not read file %s.", args[0])));
        String[] chapters = storyFromBook(bookLines.collect(Collectors.joining()))
                .flatMap(App::storyFromBook)
                .map(story -> story.split(CHAPTER_DELMITER))
                .orElseThrow(() -> error.apply("Could not find story in book."));

        Arrays.stream(chapters).map(chapter -> Arrays.stream(chapter.split(""))).map(App::clean).forEachOrdered(chapter -> {
            var warTermsCount = chapter.filter(warTerms::contains).count();
            var peaceTermsCount = chapter.filter(peaceTerms::contains).count();

            log(LogLevel.INFO, String.format("War terms: %d, Peace terms: %d", warTermsCount, peaceTermsCount));
        });
    }

    private static Optional<String> storyFromBook(String book) {
        var bookParts = book.split(BEGIN_END_INDICATOR);

        if (bookParts.length < 2) {
            return Optional.empty();
        }

        return Optional.of(bookParts[1]);
    }

    private static final Stream<String> clean(Stream<String> s) {
        return s.filter(String::isBlank).map(String::trim);
    }

    public static void log(LogLevel level, String message) {
        var log = String.format("%s %s %s", LocalDateTime.now().toString(), level.name(), message);
        level.getStream().println(log);
    }
}
