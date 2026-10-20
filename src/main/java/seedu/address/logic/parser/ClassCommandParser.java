package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import seedu.address.logic.commands.ClassCommand;
import seedu.address.logic.commands.ClassDeleteCommand;
import seedu.address.logic.commands.ClassEnrolCommand;
import seedu.address.logic.commands.ClassPaymentCommand;
import seedu.address.logic.commands.ClassUnenrolCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses the subcommands under the {@code class} command family. */
public class ClassCommandParser implements Parser<ClassCommand> {

    @Override
    public ClassCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassCommand.MESSAGE_USAGE));
        }

        String[] commandParts = trimmedArgs.split("\\s+", 2);
        String subcommand = commandParts[0];
        String subcommandArgs = commandParts.length == 2 ? " " + commandParts[1] : "";

        return switch (subcommand) {
            case ClassEnrolCommand.SUBCOMMAND_WORD -> new ClassEnrolCommandParser().parse(subcommandArgs);
            case ClassUnenrolCommand.SUBCOMMAND_WORD -> new ClassUnenrolCommandParser().parse(subcommandArgs);
            case ClassDeleteCommand.SUBCOMMAND_WORD -> new ClassDeleteCommandParser().parse(subcommandArgs);
            case ClassPaymentCommand.SUBCOMMAND_WORD -> new ClassPaymentCommandParser().parse(subcommandArgs);
            default -> throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassCommand.MESSAGE_USAGE));
        };
    }
}
