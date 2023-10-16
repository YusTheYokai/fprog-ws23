package at.technikum;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class App {

    static final Function<String, Optional<List<String>>> readFile = name -> {
        try {
            return Optional.of(Files.readAllLines(Paths.get(name)));
        } catch (IOException e) {
            return Optional.empty();
        }
    };

    static final Supplier<Optional<List<String>>> readWarTerms = () -> readFile.apply("src/main/resources/war_terms.txt");
    static final Supplier<Optional<List<String>>> readPeaceTerms = () -> readFile.apply("src/main/resources/peace_terms.txt");

    public static final void main(String[] args) {

        // IMPURE //////////////////////////////////////////////////////////////

        if (args.length != 1) {
            Logger.error("Invalid number of arguments");
            System.out.println("Usage: java -jar <jar-file> <text-file>");
            System.exit(1);
        }

        var result = readFiles(args[0]);

        // IMPURE //////////////////////////////////////////////////////////////
        // PURE   //////////////////////////////////////////////////////////////

        var warTerms = trim(result.getWarTerms());
        var peaceTerms = trim(result.getPeaceTerms());

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

    private static final void checkOptionalFile(Optional<List<String>> file, String name) {
        if (file.isEmpty()) {
            Logger.error(String.format("Could not read %s", name));
            System.exit(1);
        }
    }

    // IMPURE //////////////////////////////////////////////////////////////////
    // PURE   //////////////////////////////////////////////////////////////////

    private static final List<String> trim(List<String> list) {
        return list.stream().filter(String::isEmpty).map(String::trim).toList();
    }
}
