package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.FindCommand;
import seedu.address.model.person.LessonContainsKeywordsPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.TagContainsKeywordsPredicate;

public class FindCommandParserTest {

    private FindCommandParser parser = new FindCommandParser();

    @Test
    public void parse_emptyArg_throwsParseException() {
        assertParseFailure(parser, "     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validArgs_returnsFindCommand() {
        // no leading and trailing whitespaces
        FindCommand expectedFindCommand =
                new FindCommand(new NameContainsKeywordsPredicate(List.of("Alice", "Bob")));
        assertParseSuccess(parser, "Alice Bob", expectedFindCommand);

        // multiple whitespaces between keywords
        assertParseSuccess(parser, " \n Alice \n \t Bob  \t", expectedFindCommand);
    }

    @Test
    public void parse_validTagArgs_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new TagContainsKeywordsPredicate(List.of("friends", "family")));

        assertParseSuccess(parser, "t/friends family", expectedFindCommand);
        assertParseSuccess(parser, " \n t/ friends \n \t family  \t", expectedFindCommand);
    }

    @Test
    public void parse_emptyTagArg_throwsParseException() {
        assertParseFailure(parser, "t/     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                FindCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_validLessonArgs_returnsFindCommand() {
        FindCommand expectedFindCommand =
                new FindCommand(new LessonContainsKeywordsPredicate(List.of("Monday", "1800")));

        assertParseSuccess(parser, "c/Monday 1800", expectedFindCommand);
        assertParseSuccess(parser, " \n c/ Monday \n \t 1800  \t", expectedFindCommand);
    }

    @Test
    public void parse_emptyLessonArg_throwsParseException() {
        assertParseFailure(parser, "c/     ", String.format(MESSAGE_INVALID_COMMAND_FORMAT,
                FindCommand.MESSAGE_USAGE));
    }

}
