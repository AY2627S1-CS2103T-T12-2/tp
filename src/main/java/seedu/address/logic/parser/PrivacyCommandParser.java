package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.PrivacyCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses input arguments and creates a new {@code PrivacyCommand} object. */
public class PrivacyCommandParser implements Parser<PrivacyCommand> {

    @Override
    public PrivacyCommand parse(String args) throws ParseException {
        String mode = args.trim();
        return switch (mode) {
            case PrivacyCommand.MODE_ON -> new PrivacyCommand(true);
            case PrivacyCommand.MODE_OFF -> new PrivacyCommand(false);
            default -> throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, PrivacyCommand.MESSAGE_USAGE));
        };
    }
}
