package at.technikum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class App {

    
    private static final int DEFAULT_THREAD_COUNT = 4;
    private static final String BEGIN_END_INDICATOR = "^\\*\\*\\*.*\\*\\*\\*$";
    private static final String CHAPTER_DELMITER = "CHAPTER";

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

        // IMPURE //////////////////////////////////////////////////////////////

        if (args.length == 1 || args.length == 2) {
            Logger.error("Invalid number of arguments");
            System.out.println("Usage: java -jar <jar-file> <text-file> <thread-count>");
            System.exit(1);
        }

        var result = readFiles(args[0]);

        // IMPURE //////////////////////////////////////////////////////////////
        // PURE   //////////////////////////////////////////////////////////////

        var threadCount = args.length == 1 ? DEFAULT_THREAD_COUNT :  parseInt(args[1]).orElse(DEFAULT_THREAD_COUNT);

        var warTerms = clean(result.getWarTerms());
        var peaceTerms = clean(result.getPeaceTerms());

        // Handle optional and exit application in pure way?
        var story = storyFromBook(clean(result.getText()).collect(Collectors.joining()));

        var chapters = story.split(CHAPTER_DELMITER);

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        ChapterClassification[] classifications = new ChapterClassification[chapters.length];

        Arrays.stream(chapters).flatMap(Stream::of).skip(1).forEach(chapter -> {
            var warTermsCount = warTerms.filter(chapter::contains).count();
            var peaceTermsCount = peaceTerms.filter(chapter::contains).count();

            Logger.info(String.format("Chapter %d: %d war terms, %d peace terms", chapter.charAt(0), warTermsCount, peaceTermsCount));
        });

        // PURE   //////////////////////////////////////////////////////////////
        // IMPURE //////////////////////////////////////////////////////////////
    }

    // IMPURE //////////////////////////////////////////////////////////////////

    private static final FileReadingResult readFiles(String textFile) {
        var text = readFile.apply(textFile);
        checkOptionalFile(text, textFile);

        var warTerms = readWarTerms.get();
        checkOptionalFile(warTerms, "war terms");

        var peaceTerms = readPeaceTerms.get();
        checkOptionalFile(peaceTerms, "peace terms");

        return new FileReadingResult(text.get(), warTerms.get(), peaceTerms.get());
    }

    private static final void checkOptionalFile(Optional<Stream<String>> file, String name) {
        if (file.isEmpty()) {
            Logger.error(String.format("Could not read %s", name));
            System.exit(1);
        }
    }

    // IMPURE //////////////////////////////////////////////////////////////////
    // PURE   //////////////////////////////////////////////////////////////////

    private static Optional<Integer> parseInt(String s) {
        try {
            return Optional.of(Integer.parseInt(s));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
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
}
