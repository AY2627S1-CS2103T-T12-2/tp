package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;

import java.util.Arrays;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.TagCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new {@link TagCommand} object.
 */
public class TagCommandParser implements Parser<TagCommand> {

    @Override
    public TagCommand parse(String args) throws ParseException {
        String[] arguments = args.trim().split("\\s+");
        if (arguments.length < 2) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE));
        }

        Index index;
        try {
            index = ParserUtil.parseIndex(arguments[0]);
        } catch (ParseException pe) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE), pe);
        }
        Set<Tag> tags = ParserUtil.parseTags(Arrays.asList(arguments).subList(1, arguments.length));
        return new TagCommand(index, tags);
    }
}
