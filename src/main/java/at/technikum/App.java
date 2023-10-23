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
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {

    private static final String CHAPTER_REGEX = "CHAPTER \\d{1,2} (.*?) CHAPTER";

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
            pureMain(args).map(ChapterClassification::name).forEach(System.out::println);
        } catch (LogLeveledException e) {
            Arrays.stream(e.getMessages())
                    .forEachOrdered(message -> log(e.getLogLevel(), message));
        }
    }

    private static Stream<ChapterClassification> pureMain(String[] args) throws LogLeveledException {
        if (args.length != 1) {
            throw new LogLeveledException(LogLevel.ERROR, "Invalid number of arguments.", "Usage: java -jar <jar-file> <text-file>");
        }

        List<String> warTerms = readWarTerms.get()
                .map(clean)
                .orElseThrow(() -> fatal.apply("Could not read war terms."))
                .toList();

        List<String> peaceTerms = readPeaceTerms.get()
                .map(clean)
                .orElseThrow(() -> fatal.apply("Could not read peace terms."))
                .toList();

        Stream<String> chapters = readFile.apply(args[0])
                .map(clean)
                .map(lines -> lines.collect(Collectors.joining(" ")))
                .map(chaptersFromBook)
                .orElseThrow(() -> error.apply(String.format("Could not read file %s.", args[0])));

        try {
            Files.write(Paths.get("src/main/resources/out.txt"), chapters.collect(Collectors.toList()));
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return chapters.map(chapter -> Arrays.stream(chapter.split(" "))).map(c -> classifyChapter.apply(c, warTerms, peaceTerms));
    }

    private static final TriFunction<
        Stream<String>,
        List<String>,
        List<String>,
        ChapterClassification
    > classifyChapter = (chapter, warTerms, peaceTerms) -> {
        chapter = chapter.map(str -> str.replaceAll("\\W", "").toLowerCase());

        // TODO; This is a workaround for the fact that the stream is consumed by the first call to count().
        List<String> c = chapter.toList();
        var warTermsCount = c.stream().filter(warTerms::contains).count();
        var peaceTermsCount = c.stream().filter(peaceTerms::contains).count();

        return warTermsCount > peaceTermsCount ? ChapterClassification.WAR : ChapterClassification.PEACE;
    };

    private static final Function<String, Stream<String>> chaptersFromBook = s -> {
        var pattern = Pattern.compile(CHAPTER_REGEX, Pattern.MULTILINE);
        var matcher = pattern.matcher(s);
        return matcher.results().map(result -> result.group(1));
    };

    private static final UnaryOperator<Stream<String>> clean = s -> s.filter(str -> !str.isBlank()).map(String::trim);

    public static void log(LogLevel level, String message) {
        var log = String.format("%s %s %s", LocalDateTime.now().toString(), level.name(), message);
        level.getStream().println(log);
    }
}
