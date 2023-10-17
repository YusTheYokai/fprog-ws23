package at.technikum;

import java.util.stream.Stream;

public class FileReadingResult {

    private final Stream<String> text;
    private final Stream<String> warTerms;
    private final Stream<String> peaceTerms;

    // /////////////////////////////////////////////////////////////////////////
    // Init
    // /////////////////////////////////////////////////////////////////////////

    public FileReadingResult(Stream<String> text, Stream<String> warTerms, Stream<String> peaceTerms) {
        this.text = text;
        this.warTerms = warTerms;
        this.peaceTerms = peaceTerms;
    }

    // /////////////////////////////////////////////////////////////////////////
    // Getters
    // /////////////////////////////////////////////////////////////////////////

    public Stream<String> getText() {
        return text;
    }

    public Stream<String> getWarTerms() {
        return warTerms;
    }

    public Stream<String> getPeaceTerms() {
        return peaceTerms;
    }
}
