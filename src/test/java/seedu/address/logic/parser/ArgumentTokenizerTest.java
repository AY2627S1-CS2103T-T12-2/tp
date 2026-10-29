package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.parser.exceptions.ParseException;

public class ArgumentTokenizerTest {

    private final Prefix unknownPrefix = new Prefix("--u");
    private final Prefix pSlash = new Prefix("p/");
    private final Prefix dashT = new Prefix("-t");
    private final Prefix hatQ = new Prefix("^Q");

    @Test
    public void tokenize_emptyArgsString_noValues() {
        String argsString = "  ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);

        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);
    }

    private void assertPreamblePresent(ArgumentMultimap argMultimap, String expectedPreamble) {
        assertEquals(expectedPreamble, argMultimap.getPreamble());
    }

    private void assertPreambleEmpty(ArgumentMultimap argMultimap) {
        assertTrue(argMultimap.getPreamble().isEmpty());
    }

    /**
     * Asserts all the arguments in {@code argMultimap} with {@code prefix} match the {@code expectedValues}
     * and only the last value is returned upon calling {@code ArgumentMultimap#getValue(Prefix)}.
     */
    private void assertArgumentPresent(ArgumentMultimap argMultimap, Prefix prefix, String... expectedValues) {

        // Verify the last value is returned
        assertEquals(expectedValues[expectedValues.length - 1], argMultimap.getValue(prefix).get());

        // Verify the number of values returned is as expected
        assertEquals(expectedValues.length, argMultimap.getAllValues(prefix).size());

        // Verify all values returned are as expected and in order
        for (int i = 0; i < expectedValues.length; i++) {
            assertEquals(expectedValues[i], argMultimap.getAllValues(prefix).get(i));
        }
    }

    private void assertArgumentAbsent(ArgumentMultimap argMultimap, Prefix prefix) {
        assertFalse(argMultimap.getValue(prefix).isPresent());
    }

    @Test
    public void tokenize_noPrefixes_allTakenAsPreamble() {
        String argsString = "  some random string /t tag with leading and trailing spaces ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString);

        // Same string expected as preamble, but leading/trailing spaces should be trimmed
        assertPreamblePresent(argMultimap, argsString.trim());

    }

    @Test
    public void tokenize_oneArgument() {
        // Preamble present
        String argsString = "  Some preamble string p/ Argument value ";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreamblePresent(argMultimap, "Some preamble string");
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

        // No preamble
        argsString = " p/   Argument value ";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash);
        assertPreambleEmpty(argMultimap);
        assertArgumentPresent(argMultimap, pSlash, "Argument value");

    }

    @Test
    public void tokenize_multipleArguments() {
        // Only two arguments are present
        String argsString = "SomePreambleString -t dashT-Value p/pSlash value";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentAbsent(argMultimap, hatQ);

        // All three arguments are present
        argsString = "Different Preamble String ^Q111 -t dashT-Value p/pSlash value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "Different Preamble String");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value");
        assertArgumentPresent(argMultimap, hatQ, "111");

        /* Also covers: Reusing of the tokenizer multiple times */

        // Reuse tokenizer on an empty string to ensure ArgumentMultimap is correctly reset
        // (i.e. no stale values from the previous tokenizing remain)
        argsString = "";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreambleEmpty(argMultimap);
        assertArgumentAbsent(argMultimap, pSlash);

        /* Also covers: testing for prefixes not specified as a prefix */

        // Prefixes not previously given to the tokenizer should not return any values
        argsString = unknownPrefix + "some value";
        argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertArgumentAbsent(argMultimap, unknownPrefix);
        assertPreamblePresent(argMultimap, argsString); // Unknown prefix is taken as part of preamble
    }

    @Test
    public void tokenize_multipleArgumentsWithRepeats() {
        // Two arguments repeated, some have empty values
        String argsString = "SomePreambleString -t dashT-Value ^Q ^Q -t another dashT value p/ pSlash value -t";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleString");
        assertArgumentPresent(argMultimap, pSlash, "pSlash value");
        assertArgumentPresent(argMultimap, dashT, "dashT-Value", "another dashT value", "");
        assertArgumentPresent(argMultimap, hatQ, "", "");
    }

    @Test
    public void tokenize_multipleArgumentsJoined() {
        String argsString = "SomePreambleStringp/ pSlash joined-tjoined -t not joined^Qjoined";
        ArgumentMultimap argMultimap = ArgumentTokenizer.tokenize(argsString, pSlash, dashT, hatQ);
        assertPreamblePresent(argMultimap, "SomePreambleStringp/ pSlash joined-tjoined");
        assertArgumentAbsent(argMultimap, pSlash);
        assertArgumentPresent(argMultimap, dashT, "not joined^Qjoined");
        assertArgumentAbsent(argMultimap, hatQ);
    }

    @Test
    public void tokenize_whitespaceSeparatedPrefixes_recognizedWithoutSplittingEmbeddedText() {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(
                "\tp/one\np/two\rp/three p/embeddedp/value", pSlash);
        assertPreambleEmpty(arguments);
        assertArgumentPresent(arguments, pSlash, "one", "two", "three", "embeddedp/value");
    }

    @Test
    public void tokenizeWithFreeTextFields_quotedPrefixes_preservedLiterally() throws Exception {
        Prefix notes = new Prefix("notes/");
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeWithFreeTextFields(
                " p/first notes/\"Review p/third notes/extra modee/online\" p/second",
                Set.of(notes), Set.of(), pSlash, notes);

        assertPreambleEmpty(arguments);
        assertArgumentPresent(arguments, pSlash, "first", "second");
        assertArgumentPresent(arguments, notes, "Review p/third notes/extra modee/online");
    }

    @Test
    public void tokenizeWithFreeTextFields_unknownPrefixLikeToken_rejected() {
        Prefix notes = new Prefix("notes/");
        ParseException exception = assertThrows(ParseException.class, () ->
                ArgumentTokenizer.tokenizeWithFreeTextFields(
                        " notes/Review modee/online", Set.of(notes), Set.of(), notes, pSlash));

        assertEquals("Unrecognised parameter-like token: modee/", exception.getMessage());
    }

    @Test
    public void tokenizeWithFreeTextFields_absentOrEmptyText_keepsOtherArguments() throws Exception {
        Prefix notes = new Prefix("notes/");
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeWithFreeTextFields(
                " p/first", Set.of(notes), Set.of(), pSlash, notes);
        assertArgumentPresent(arguments, pSlash, "first");
        assertArgumentAbsent(arguments, notes);

        arguments = ArgumentTokenizer.tokenizeWithFreeTextFields(
                " notes/\"\" p/first", Set.of(notes), Set.of(), pSlash, notes);
        assertArgumentPresent(arguments, pSlash, "first");
        assertArgumentPresent(arguments, notes, "");
    }

    @Test
    public void tokenizeWithFreeTextFields_escapedQuotesAndBackslashes_preserved() throws Exception {
        Prefix notes = new Prefix("notes/");
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeWithFreeTextFields(
                " notes/\"Review \\\"p/literal\\\" at C:\\\\notes\" p/123", Set.of(notes), Set.of(), notes, pSlash);
        assertArgumentPresent(arguments, notes, "Review \"p/literal\" at C:\\notes");
        assertArgumentPresent(arguments, pSlash, "123");
    }

    @Test
    public void tokenizeWithFreeTextFields_malformedQuotes_rejected() {
        Prefix notes = new Prefix("notes/");
        for (String input : new String[] {" notes/\"unfinished", " notes/\"Review\" extra p/123",
            " notes/\"Review\"p/123"}) {
            assertThrows(ParseException.class, () -> ArgumentTokenizer.tokenizeWithFreeTextFields(
                    input, Set.of(notes), Set.of(), notes, pSlash));
        }
    }

    @Test
    public void equalsMethod() {
        Prefix aaa = new Prefix("aaa");

        assertEquals(aaa, aaa);
        assertEquals(aaa, new Prefix("aaa"));

        assertNotEquals(aaa, "aaa");
        assertNotEquals(aaa, new Prefix("aab"));
    }

}
