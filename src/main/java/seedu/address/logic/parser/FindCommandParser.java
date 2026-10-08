package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.List;

import seedu.address.logic.commands.FindCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.TagContainsKeywordsPredicate;

/**
 * Parses input arguments and creates a new FindCommand object
 */
public class FindCommandParser implements Parser<FindCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the FindCommand
     * and returns a FindCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public FindCommand parse(String args) throws ParseException {
        String trimmedArgs = args.trim();
        if (trimmedArgs.isEmpty()) {
            throw new ParseException(
                    String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
        }

        if (trimmedArgs.startsWith(PREFIX_TAG.toString()) || trimmedArgs.startsWith(PREFIX_CLASS.toString())) {
            ArgumentMultimap arguments = ArgumentTokenizer.tokenize(" " + trimmedArgs, PREFIX_TAG, PREFIX_CLASS);
            boolean hasTag = arguments.getValue(PREFIX_TAG).isPresent();
            boolean hasClass = arguments.getValue(PREFIX_CLASS).isPresent();
            if (hasTag == hasClass || !arguments.getPreamble().isEmpty()) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
            }
            arguments.verifyNoDuplicatePrefixesFor(PREFIX_TAG, PREFIX_CLASS);
            if (hasTag) {
                String tagArguments = arguments.getValue(PREFIX_TAG).get().trim();
                if (tagArguments.isEmpty()) {
                    throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, FindCommand.MESSAGE_USAGE));
                }
                List<String> tagKeywords = List.of(tagArguments.split("\\s+"));
                for (String keyword : tagKeywords) {
                    ParserUtil.parseTag(keyword);
                }
                return new FindCommand(new TagContainsKeywordsPredicate(tagKeywords));
            }
            return new FindCommand(ParserUtil.parseLessonStart(arguments.getValue(PREFIX_CLASS).get()));
        }

        String[] nameKeywords = trimmedArgs.split("\\s+");

        return new FindCommand(new NameContainsKeywordsPredicate(List.of(nameKeywords)));
    }

}
