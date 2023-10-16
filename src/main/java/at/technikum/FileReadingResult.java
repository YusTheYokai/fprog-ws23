package at.technikum;

import java.util.List;

public class FileReadingResult {

    private final List<String> text;
    private final List<String> warTerms;
    private final List<String> peaceTerms;

    // /////////////////////////////////////////////////////////////////////////
    // Init
    // /////////////////////////////////////////////////////////////////////////

    public FileReadingResult(List<String> text, List<String> warTerms, List<String> peaceTerms) {
        this.text = text;
        this.warTerms = warTerms;
        this.peaceTerms = peaceTerms;
    }

    // /////////////////////////////////////////////////////////////////////////
    // Getters
    // /////////////////////////////////////////////////////////////////////////

    public List<String> getText() {
        return text;
    }

    public List<String> getWarTerms() {
        return warTerms;
    }

    public List<String> getPeaceTerms() {
        return peaceTerms;
    }
}
