package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseFailure;
import static seedu.address.logic.parser.CommandParserTestUtil.assertParseSuccess;

import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.PrivacyCommand;

public class PrivacyCommandParserTest {

    private final PrivacyCommandParser parser = new PrivacyCommandParser();

    @Test
    public void parse_validMode_returnsPrivacyCommand() {
        assertParseSuccess(parser, " on ", new PrivacyCommand(true));
        assertParseSuccess(parser, " off ", new PrivacyCommand(false));
    }

    @Test
    public void parse_invalidMode_throwsParseException() {
        String expectedMessage = String.format(MESSAGE_INVALID_COMMAND_FORMAT, PrivacyCommand.MESSAGE_USAGE);

        assertParseFailure(parser, "", expectedMessage);
        assertParseFailure(parser, "enable", expectedMessage);
        assertParseFailure(parser, "ON", expectedMessage);
        assertParseFailure(parser, "on now", expectedMessage);
    }
}
