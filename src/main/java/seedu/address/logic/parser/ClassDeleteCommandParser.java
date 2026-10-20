package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import seedu.address.logic.commands.ClassDeleteCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.Subject;

/** Parses arguments for the {@code class delete} command. */
public class ClassDeleteCommandParser implements Parser<ClassDeleteCommand> {

    @Override
    public ClassDeleteCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_SUBJECT, PREFIX_CLASS);
        if (!arguments.getPreamble().isEmpty() || arguments.getValue(PREFIX_SUBJECT).isEmpty()
                || arguments.getValue(PREFIX_CLASS).isEmpty()) {
            throw new ParseException(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT, ClassDeleteCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_SUBJECT, PREFIX_CLASS);

        Subject subject = ParserUtil.parseLessonSubject(arguments.getValue(PREFIX_SUBJECT).get());
        LessonSchedule schedule = ParserUtil.parseLessonSchedule(arguments.getValue(PREFIX_CLASS).get());
        return new ClassDeleteCommand(subject, schedule);
    }
}
