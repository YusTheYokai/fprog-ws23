package at.technikum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class App {

    /**
     * Captures the text between chapter headlines.
     * The lookahead is used to not consume the next chapter headline.
     * Need to check for END OF THE PROJECT GUTENBERG EBOOK as the last
     * chapter is not followed by another chapter headline.
     */
    private static final String CHAPTER_REGEX = "CHAPTER \\d{1,2} (.*?) (?=(CHAPTER)|(\\*\\*\\* END OF THE PROJECT GUTENBERG EBOOK))";

    private static final Function<String, Try<Stream<String>>> readFile = name -> {
        try {
            return Try.success(Files.lines(Paths.get(name)));
        } catch (IOException e) {
            return Try.failure(e);
        }
    };

    private static final Supplier<Try<Stream<String>>> readWarTerms = () -> readFile.apply("src/main/resources/war_terms.txt");
    private static final Supplier<Try<Stream<String>>> readPeaceTerms = () -> readFile.apply("src/main/resources/peace_terms.txt");

    private static final UnaryOperator<Stream<String>> clean = s -> s.filter(str -> !str.isBlank()).map(String::trim);
    private static final Function<String, Stream<String>> mapToChapters = s -> {
        var pattern = Pattern.compile(CHAPTER_REGEX, Pattern.MULTILINE);
        var matcher = pattern.matcher(s);
        return matcher.results().map(result -> result.group(1));
    };

    private static final Function<Stream<String>, String> join = s -> s.collect(Collectors.joining(" "));
    private static final UnaryOperator<Stream<String>> getChapters = textLines -> clean.andThen(join).andThen(mapToChapters).apply(textLines);

    private static final Function<List<String>, Function<List<String>, Function<Stream<String>, Optional<ChapterClassification>>>> classifyChapter = warTerms -> peaceTerms -> chapter -> {
        // Remove all non word characters and convert to lowercase.
        chapter = chapter.map(str -> str.replaceAll("\\W", "").toLowerCase());

        // This is a workaround for the fact that
        // the stream is consumed by the first call to count.
        List<String> words = chapter.toList();
        var warTermsCount = words.stream().filter(warTerms::contains).count();
        var peaceTermsCount = words.stream().filter(peaceTerms::contains).count();
        var warTermDistances = IntStream.range(0, words.size()).mapToObj(i -> new Pair<Integer, String>(i, words.get(i))).filter(p -> warTerms.contains(p.getSecond())).mapToInt(Pair::getFirst).reduce((left, right) -> right - left);
        var peaceTermDistances = IntStream.range(0, words.size()).mapToObj(i -> new Pair<Integer, String>(i, words.get(i))).filter(p -> peaceTerms.contains(p.getSecond())).mapToInt(Pair::getFirst).reduce((left, right) -> right - left);

        if (warTermDistances.isEmpty() && peaceTermDistances.isEmpty()) {
            return Optional.empty();
        } else if (warTermDistances.isEmpty()) {
            return Optional.of(ChapterClassification.PEACE);
        } else if (peaceTermDistances.isEmpty()) {
            return Optional.of(ChapterClassification.WAR);
        }

        return Optional.of(warTermDistances.getAsInt() / warTermsCount > peaceTermDistances.getAsInt() / peaceTermsCount ? ChapterClassification.WAR : ChapterClassification.PEACE);
    };

    private static Function<String[], Either<Exception, Stream<Optional<ChapterClassification>>>> pureMain = args -> {
        if (args.length != 1) {
            return Either.left(new Exception("Invalid number of arguments. Usage: java -jar <jar-file> <text-file>."));
        }

        var warTermsTry = readWarTerms.get();
        if (warTermsTry.isFailure()) {
            return Either.left(warTermsTry.getException());
        }

        var peaceTermsTry = readPeaceTerms.get();
        if (peaceTermsTry.isFailure()) {
            return Either.left(peaceTermsTry.getException());
        }

        var textTry = readFile.apply(args[0]);
        if (textTry.isFailure()) {
            return Either.left(textTry.getException());
        }

        var warTerms = clean.apply(warTermsTry.get()).toList();
        var peaceTerms = clean.apply(peaceTermsTry.get()).toList();

        var classify = classifyChapter.apply(warTerms).apply(peaceTerms);

        return Either.right(getChapters.apply(textTry.get()).map(chapter -> Arrays.stream(chapter.split(" "))).map(classify));
    };

    public static final void main(String[] args) {
        var result = pureMain.apply(args);

        if (result.isLeft()) {
            System.out.println(result.getLeft().getMessage());
            System.exit(1);
        }

        AtomicInteger chapterCount = new AtomicInteger(1);
        result.getRight()
                .map(o -> o.orElseGet(() -> ChapterClassification.NONE))
                .map(c -> String.format("Chapter %d: %s", chapterCount.getAndIncrement(), c.name())).forEachOrdered(System.out::println);
    }
}
