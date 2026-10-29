package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ClassUnenrolCommand;
import seedu.address.logic.parser.exceptions.ParseException;

/** Parses arguments for the {@code class unenrol} command. */
public class ClassUnenrolCommandParser implements Parser<ClassUnenrolCommand> {

    public static final String MESSAGE_REQUIRES_ONE_STUDENT =
            "Students can be unenrolled one at a time. Provide exactly one student index.";

    @Override
    public ClassUnenrolCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_CLASS_ID, PREFIX_SUBJECT, PREFIX_CLASS);
        boolean hasId = arguments.getValue(PREFIX_CLASS_ID).isPresent();
        boolean hasSubject = arguments.getValue(PREFIX_SUBJECT).isPresent();
        boolean hasTiming = arguments.getValue(PREFIX_CLASS).isPresent();
        boolean usesOnlyId = hasId && !hasSubject && !hasTiming;
        boolean usesOnlyLegacyDetails = !hasId && hasSubject && hasTiming;
        if (arguments.getPreamble().isEmpty() || usesOnlyId == usesOnlyLegacyDetails) {
            throw new ParseException(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT, ClassUnenrolCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLASS_ID, PREFIX_SUBJECT, PREFIX_CLASS);

        List<Index> indexes = ParserUtil.parseIndexes(arguments.getPreamble());
        if (indexes.size() != 1) {
            throw new ParseException(MESSAGE_REQUIRES_ONE_STUDENT);
        }
        Index index = indexes.get(0);
        if (usesOnlyId) {
            return new ClassUnenrolCommand(index, ParserUtil.parseClassId(arguments.getValue(PREFIX_CLASS_ID).get()));
        }
        return new ClassUnenrolCommand(index,
                ParserUtil.parseLessonSubject(arguments.getValue(PREFIX_SUBJECT).get()),
                ParserUtil.parseLessonSchedule(arguments.getValue(PREFIX_CLASS).get()));
    }
}
