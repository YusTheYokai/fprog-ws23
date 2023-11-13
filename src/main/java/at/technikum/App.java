package at.technikum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {

    /**
     * Captures the text between chapter headlines.
     * The lookahead is used to not consume the next chapter headline.
     * Need to check for END OF THE PROJECT GUTENBERG EBOOK as the last
     * chapter is not followed by another chapter headline.
     */
    private static final String CHAPTER_REGEX = "CHAPTER \\d{1,2} (.*?) (?=(CHAPTER)|(\\*\\*\\* END OF THE PROJECT GUTENBERG EBOOK))";

    // /////////////////////////////////////////////////////////////////////////
    // I/O
    // /////////////////////////////////////////////////////////////////////////

    /**
     * Reads a file and returns a Try of a Stream of lines.
     */
    public static final Function<String, Try<Stream<String>>> readFile = name -> {
        try {
            return Try.success(Files.lines(Paths.get(name)));
        } catch (IOException e) {
            return Try.failure(e);
        }
    };

    /**
     * Reads the war terms file.
     * @see readFile
     */
    private static final Supplier<Try<Stream<String>>> readWarTerms = () -> readFile.apply("src/main/resources/war_terms.txt");

    /**
     * Reads the peace terms file.
     * @see readFile
     */
    private static final Supplier<Try<Stream<String>>> readPeaceTerms = () -> readFile.apply("src/main/resources/peace_terms.txt");

    // /////////////////////////////////////////////////////////////////////////
    // TEXT PROCESSING
    // /////////////////////////////////////////////////////////////////////////

    /**
     * Removes blank lines and trims the lines.
     */
    public static final UnaryOperator<Stream<String>> clean = s -> s.filter(str -> !str.isBlank()).map(String::trim);

    /**
     * Extracts the chapters from the text.
     */
    public static final Function<String, Stream<String>> mapToChapters = s -> {
        var pattern = Pattern.compile(CHAPTER_REGEX, Pattern.MULTILINE);
        var matcher = pattern.matcher(s);
        return matcher.results().map(result -> result.group(1));
    };

    /**
     * Joins a stream of strings using space as the delimiter.
     */
    public static final Function<Stream<String>, String> join = s -> s.collect(Collectors.joining(" "));

    /**
     * Maps a stream of strings to a stream of chapters.
     * @see clean
     * @see join
     * @see mapToChapters
     */
    private static final UnaryOperator<Stream<String>> getChapters = textLines -> clean.andThen(join).andThen(mapToChapters).apply(textLines);

    /**
     * Supplies a stream of all words that are contained in filterList.
     */
    public static final Function<
        List<String>,
        Function<
            List<String>,
            Stream<String>
        >
    > getFilteredWordStream = words -> filterList -> words.stream().filter(filterList::contains);

    /**
     * Classifies a chapter as WAR or PEACE using the count of war and peace terms.
     */
    public static final Function<
        List<String>,
        Function<
            List<String>,
            Function<
                Stream<String>,
                Classification>
            >
        > classifyChapter = warTerms -> peaceTerms -> chapter -> {
        var cleanedChapter = chapter.map(str -> str.replaceAll("\\W", "").toLowerCase());

        var curriedGetFilteredWordStream = getFilteredWordStream.apply(cleanedChapter.toList());
        var warTermsCount = curriedGetFilteredWordStream.apply(warTerms).count();
        var peaceTermsCount = curriedGetFilteredWordStream.apply(peaceTerms).count();

        return warTermsCount > peaceTermsCount ? Classification.WAR : Classification.PEACE;
    };

    /**
     * Splits a string at spaces.
     */
    public static final Function<String, Stream<String>> splitAtSpace = s -> Arrays.stream(s.split(" "));

    // /////////////////////////////////////////////////////////////////////////
    // MAIN
    // /////////////////////////////////////////////////////////////////////////

    private static final Function<String[], Either<Exception, Stream<Classification>>> pureMain = args -> {
        if (args.length != 1) {
            return Either.left(new Exception("Invalid number of arguments. Usage: java -jar <jar-file> <text-file>"));
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

        return Either.right(getChapters.apply(textTry.get()).map(splitAtSpace).map(classify));
    };

    /**
     * Maps a classification to a string with chapter index.
     */
    public static final Function<AtomicInteger, Function<Classification, String>> mapToChapterString =
            chapterCount -> classification -> String.format("Chapter %d: %s", chapterCount.getAndIncrement(), classification.name());

    public static final void main(String[] args) {
        var result = pureMain.apply(args);

        if (result.isLeft()) {
            System.out.println(result.getLeft().getMessage());
            System.exit(1);
        }

        var stringMapper = mapToChapterString.apply(new AtomicInteger(1));
        result.getRight()
                .map(stringMapper)
                .forEachOrdered(System.out::println);
    }
}
