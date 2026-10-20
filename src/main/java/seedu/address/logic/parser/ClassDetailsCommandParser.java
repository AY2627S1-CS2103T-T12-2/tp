package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LINK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import seedu.address.logic.commands.ClassDetailsCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;

/** Parses updates to the shared details of a class. */
public class ClassDetailsCommandParser implements Parser<ClassDetailsCommand> {

    @Override
    public ClassDetailsCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args,
                PREFIX_SUBJECT, PREFIX_CLASS, PREFIX_LINK, PREFIX_LOCATION, PREFIX_NOTES);
        arguments.verifyNoDuplicatePrefixesFor(
                PREFIX_SUBJECT, PREFIX_CLASS, PREFIX_LINK, PREFIX_LOCATION, PREFIX_NOTES);
        if (!arguments.getPreamble().isEmpty() || arguments.getValue(PREFIX_SUBJECT).isEmpty()
                || arguments.getValue(PREFIX_CLASS).isEmpty()
                || (arguments.getValue(PREFIX_LINK).isEmpty() && arguments.getValue(PREFIX_LOCATION).isEmpty()
                && arguments.getValue(PREFIX_NOTES).isEmpty())) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassDetailsCommand.MESSAGE_USAGE));
        }
        Subject subject = ParserUtil.parseLessonSubject(arguments.getValue(PREFIX_SUBJECT).get());
        LessonTiming timing = ParserUtil.parseLessonTiming(arguments.getValue(PREFIX_CLASS).get());
        try {
            return new ClassDetailsCommand(subject, timing, arguments.getValue(PREFIX_LINK),
                    arguments.getValue(PREFIX_LOCATION), arguments.getValue(PREFIX_NOTES));
        } catch (IllegalArgumentException e) {
            throw new ParseException(e.getMessage(), e);
        }
    }
}
