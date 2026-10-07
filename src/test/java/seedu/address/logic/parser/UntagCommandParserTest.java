package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;
import static seedu.address.testutil.TypicalIndexes.INDEX_FIRST_PERSON;

import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.UntagCommand;
import seedu.address.model.tag.Tag;

public class UntagCommandParserTest {

    private final UntagCommandParser parser = new UntagCommandParser();

    @Test
    public void parse_validTag_returnsUntagCommand() {
        assertParseSuccess(parser, "1 parent-follow-up",
                new UntagCommand(INDEX_FIRST_PERSON, Set.of(new Tag("parent-follow-up"))));
    }

    @Test
    public void parse_removeAllFlag_returnsUntagCommand() {
        assertParseSuccess(parser, "1 -all", new UntagCommand(INDEX_FIRST_PERSON));
    }

    @Test
    public void parse_doubleDashAll_throwsParseException() {
        assertParseFailure(parser, "1 --all",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, UntagCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_missingTag_throwsParseException() {
        assertParseFailure(parser, "1", String.format(MESSAGE_INVALID_COMMAND_FORMAT, UntagCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidIndex_throwsParseException() {
        assertParseFailure(parser, "a friends",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, UntagCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_removeAllWithTag_throwsParseException() {
        assertParseFailure(parser, "1 -all friends",
                String.format(MESSAGE_INVALID_COMMAND_FORMAT, UntagCommand.MESSAGE_USAGE));
    }

    @Test
    public void parse_invalidTag_throwsParseException() {
        assertParseFailure(parser, "1 parent_follow_up", Tag.MESSAGE_CONSTRAINTS);
    }
}
