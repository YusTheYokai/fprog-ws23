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

    public static void main(String[] args) {
        var warTerms = readWarTerms.get();
        var peaceTerms = readPeaceTerms.get();
    }
}
