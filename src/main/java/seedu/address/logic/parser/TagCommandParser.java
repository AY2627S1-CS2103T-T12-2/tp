package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
        ArgumentMultimap argumentMultimap = ArgumentTokenizer.tokenize(args, PREFIX_TAG);
        if (argumentMultimap.getValue(PREFIX_TAG).isPresent()) {
            return parseBatchCommand(argumentMultimap);
        }
        return parseSingleStudentCommand(args);
    }

    private TagCommand parseSingleStudentCommand(String args) throws ParseException {
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

    private TagCommand parseBatchCommand(ArgumentMultimap argumentMultimap) throws ParseException {
        argumentMultimap.verifyNoDuplicatePrefixesFor(PREFIX_TAG);
        String preamble = argumentMultimap.getPreamble();
        if (preamble.isBlank()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, TagCommand.MESSAGE_USAGE));
        }

        List<Index> indexes = parseUniqueIndexes(preamble);
        Tag tag = ParserUtil.parseTag(argumentMultimap.getValue(PREFIX_TAG).orElse(""));
        return new TagCommand(indexes, tag);
    }

    private List<Index> parseUniqueIndexes(String preamble) throws ParseException {
        List<Index> indexes = new ArrayList<>();
        for (String indexText : preamble.trim().split("\\s+")) {
            Index index = ParserUtil.parseIndex(indexText);
            boolean isDuplicate = indexes.stream()
                    .anyMatch(existing -> existing.getZeroBased() == index.getZeroBased());
            if (isDuplicate) {
                throw new ParseException(TagCommand.MESSAGE_DUPLICATE_INDEXES);
            }
            indexes.add(index);
        }
        return List.copyOf(indexes);
    }
}
