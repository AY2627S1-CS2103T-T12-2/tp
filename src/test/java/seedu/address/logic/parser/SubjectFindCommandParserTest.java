package seedu.address.logic.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.SubjectContainsKeywordsPredicate;

public class SubjectFindCommandParserTest {

    private final SubjectFindCommandParser parser = new SubjectFindCommandParser();

    @Test
    public void parse_subjectKeywords_success() {
        FindCommand expected = new FindCommand(new SubjectContainsKeywordsPredicate(List.of("Math", "Physics")));
        assertParseSuccess(parser, "sub/Math Physics", expected);
        assertParseSuccess(parser, " \t sub/Math \n Physics \t ", expected);
    }

    @Test
    public void parse_emptySubject_failure() {
        assertParseFailure(parser, "sub/", SubjectFindCommandParser.MESSAGE_EMPTY_SUBJECT);
        assertParseFailure(parser, " sub/ \t ", SubjectFindCommandParser.MESSAGE_EMPTY_SUBJECT);
    }

    @Test
    public void parse_mixedModes_failure() {
        for (String input : List.of("Alice sub/Math", "sub/Math t/priority", "c/Monday1800 sub/Math",
                "t/priority sub/Math", "sub/Math c/Monday1800", "sub/Math\tt/priority")) {
            assertParseFailure(parser, input, SubjectFindCommandParser.MESSAGE_MIXED_MODES);
        }
    }

    @Test
    public void parse_duplicateOrInvalidSubject_failure() {
        assertParseFailure(parser, "sub/Math sub/Physics",
                seedu.address.logic.Messages.getErrorMessageForDuplicatePrefixes(CliSyntax.PREFIX_SUBJECT));
        assertParseFailure(parser, "sub/Math!", seedu.address.model.lesson.Subject.MESSAGE_CONSTRAINTS);
        assertThrows(ParseException.class, () -> parser.parse("sub/Math cost/30"));
    }

    @Test
    public void parse_numericLevelsAndRepeatedKeywords_success() {
        assertParseSuccess(parser, "sub/H2 Math Math",
                new FindCommand(new SubjectContainsKeywordsPredicate(List.of("H2", "Math", "Math"))));
    }

    @Test
    public void parse_otherModes_delegatesUnchanged() throws Exception {
        FindCommandParser existingParser = new FindCommandParser();
        for (String input : List.of("Alice Bob", "t/priority", "c/Monday1800")) {
            assertEquals(existingParser.parse(input), parser.parse(input));
        }
        for (String input : List.of("", "t/", "c/", "t/priority c/Monday1800")) {
            ParseException expected = assertThrows(ParseException.class, () -> existingParser.parse(input));
            ParseException actual = assertThrows(ParseException.class, () -> parser.parse(input));
            assertEquals(expected.getMessage(), actual.getMessage());
        }
    }

    @Test
    public void parseCommand_subject_routesThroughAddressBookParser() throws Exception {
        assertEquals(parser.parse("sub/Math"), new AddressBookParser().parseCommand("find sub/Math"));
    }
}
