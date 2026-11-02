package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LINK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.Optional;
import java.util.Set;

import seedu.address.logic.commands.ClassEditCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses {@code class edit}. */
public class ClassEditCommandParser implements Parser<ClassEditCommand> {
    @Override
    public ClassEditCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenizeWithFreeTextFields(args,
                Set.of(PREFIX_LOCATION, PREFIX_NOTES), Set.of(),
                PREFIX_CLASS_ID, PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS,
                PREFIX_MODE, PREFIX_LINK, PREFIX_LOCATION, PREFIX_NOTES);
        boolean hasEdit = arguments.getValue(PREFIX_SUBJECT).isPresent()
                || arguments.getValue(PREFIX_COST).isPresent()
                || arguments.getValue(PREFIX_CLASS).isPresent()
                || arguments.getValue(PREFIX_MODE).isPresent()
                || arguments.getValue(PREFIX_LINK).isPresent()
                || arguments.getValue(PREFIX_LOCATION).isPresent()
                || arguments.getValue(PREFIX_NOTES).isPresent();
        if (!arguments.getPreamble().isEmpty() || arguments.getValue(PREFIX_CLASS_ID).isEmpty() || !hasEdit) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassEditCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLASS_ID, PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS,
                PREFIX_MODE, PREFIX_LINK, PREFIX_LOCATION, PREFIX_NOTES);
        return new ClassEditCommand(ParserUtil.parseClassId(arguments.getValue(PREFIX_CLASS_ID).get()),
                parseOptionalSubject(arguments), parseOptionalCost(arguments), parseOptionalSchedule(arguments),
                ParserUtil.parseClassDetailsUpdate(arguments));
    }

    private static Optional<seedu.address.model.lesson.Subject> parseOptionalSubject(ArgumentMultimap arguments)
            throws ParseException {
        return arguments.getValue(PREFIX_SUBJECT).isPresent()
                ? Optional.of(ParserUtil.parseLessonSubject(arguments.getValue(PREFIX_SUBJECT).get()))
                : Optional.empty();
    }

    private static Optional<seedu.address.model.lesson.Cost> parseOptionalCost(ArgumentMultimap arguments)
            throws ParseException {
        return arguments.getValue(PREFIX_COST).isPresent()
                ? Optional.of(ParserUtil.parseLessonCost(arguments.getValue(PREFIX_COST).get()))
                : Optional.empty();
    }

    private static Optional<seedu.address.model.lesson.LessonSchedule> parseOptionalSchedule(ArgumentMultimap arguments)
            throws ParseException {
        return arguments.getValue(PREFIX_CLASS).isPresent()
                ? Optional.of(ParserUtil.parseLessonSchedule(arguments.getValue(PREFIX_CLASS).get()))
                : Optional.empty();
    }
}
