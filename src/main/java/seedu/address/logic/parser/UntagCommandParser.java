package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.UntagCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new {@link UntagCommand} object.
 */
public class UntagCommandParser implements Parser<UntagCommand> {

    @Override
    public UntagCommand parse(String args) throws ParseException {
        String[] arguments = args.trim().split("\\s+");
        if (arguments.length < 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UntagCommand.MESSAGE_USAGE));
        }

        Index index;
        try {
            index = ParserUtil.parseIndex(arguments[0]);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UntagCommand.MESSAGE_USAGE), pe);
        }
        List<String> tagArguments = Arrays.asList(arguments).subList(1, arguments.length);
        if (tagArguments.size() == 1 && isRemoveAllFlag(tagArguments.get(0))) {
            return new UntagCommand(index);
        }
        if (tagArguments.stream().anyMatch(UntagCommandParser::isRemoveAllFlag)
                || tagArguments.contains("--all")) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, UntagCommand.MESSAGE_USAGE));
        }
        Set<Tag> tags = ParserUtil.parseTags(tagArguments);
        return new UntagCommand(index, tags);
    }

    private static boolean isRemoveAllFlag(String argument) {
        return UntagCommand.REMOVE_ALL_FLAG.equals(argument);
    }
}
