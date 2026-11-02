package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LINK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.Set;

import seedu.address.logic.commands.ClassAddCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses {@code class add}. */
public class ClassAddCommandParser implements Parser<ClassAddCommand> {
    @Override
    public ClassAddCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeWithFreeTextFields(args,
                Set.of(PREFIX_LOCATION, PREFIX_NOTES), Set.of(),
                PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS, PREFIX_MODE, PREFIX_LINK, PREFIX_LOCATION, PREFIX_NOTES);
        if (!arguments.getPreamble().isEmpty() || arguments.getValue(PREFIX_SUBJECT).isEmpty()
                || arguments.getValue(PREFIX_COST).isEmpty() || arguments.getValue(PREFIX_CLASS).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassAddCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS,
                PREFIX_MODE, PREFIX_LINK, PREFIX_LOCATION, PREFIX_NOTES);
        return new ClassAddCommand(
                ParserUtil.parseLessonSubject(arguments.getValue(PREFIX_SUBJECT).get()),
                ParserUtil.parseLessonCost(arguments.getValue(PREFIX_COST).get()),
                ParserUtil.parseLessonSchedule(arguments.getValue(PREFIX_CLASS).get()),
                ParserUtil.parseClassDetailsUpdate(arguments));
    }
}
