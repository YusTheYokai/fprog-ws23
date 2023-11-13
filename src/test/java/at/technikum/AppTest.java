package at.technikum;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;

public class AppTest {

	@Test
	public void testClean() {
		// Arrange
		var stream = Arrays.asList(" a ", "  ", "b ").stream();

		// Act
		var result = App.clean.apply(stream);

		assertEquals(Arrays.asList("a", "b"), result.toList());
	}

	@Test
	public void testMapToChapters() {
		// Arrange
		var text = "irrelevant CHAPTER 1 Text CHAPTER 2 More text *** END OF THE PROJECT GUTENBERG EBOOK";

		// Act
		var stream = App.mapToChapters.apply(text);

		// Assert
		assertEquals(Arrays.asList("Text", "More text"), stream.toList());
	}

	@Test
	public void testJoin() {
		// Arrange
		var stream = Arrays.asList("a", "b", "c").stream();

		// Act
		var result = App.join.apply(stream);

		// Assert
		assertEquals("a b c", result);
	}

	@Test
	public void testGetFilteredWordStream() {
		// Arrange
		var words = Arrays.asList("bad", "This", "is", "a", "bad", "chapter", "absolutely", "horrible");
		var filterList = Arrays.asList("bad", "horrible");

		// Act
		var result = App.getFilteredWordStream.apply(words).apply(filterList);

		// Assert
		assertEquals(Arrays.asList("bad", "bad", "horrible"), result.toList());
	}

	@Test
	public void testClassifyChapter() {
		// Arrange
		var warTerms = Arrays.asList("war", "battle");
		var peaceTerms = Arrays.asList("peace", "love");
		var words = Arrays.asList("We", "are", "going", "to", "war", "there", "is", "no", "peace", "without", "battle");
		var chapter = words.stream();

		// Act
		var result = App.classifyChapter.apply(warTerms).apply(peaceTerms).apply(chapter);

		// Assert
		assertEquals(Classification.WAR, result);
	}

	@Test
	public void testSplitAtSpace() {
		// Arrange
		var text = "This is a test";

		// Act
		var result = App.splitAtSpace.apply(text);

		// Assert
		assertEquals(Arrays.asList("This", "is", "a", "test"), result.toList());
	}

	@Test
	public void testMapToChapterString() {
		// Arrange
		var count = new AtomicInteger(1);
		var classification = Classification.WAR;

		// Act
		var result = App.mapToChapterString.apply(count).apply(classification);

		// Assert
		assertEquals("Chapter 1: WAR", result);
	}
}
