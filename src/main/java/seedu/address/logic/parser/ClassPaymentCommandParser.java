package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ClassPaymentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;

/** Parses arguments for the {@code class payment} command. */
public class ClassPaymentCommandParser implements Parser<ClassPaymentCommand> {

    @Override
    public ClassPaymentCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(
                args, PREFIX_SUBJECT, PREFIX_CLASS, PREFIX_STATUS);
        if (arguments.getPreamble().isEmpty() || arguments.getValue(PREFIX_SUBJECT).isEmpty()
                || arguments.getValue(PREFIX_CLASS).isEmpty()
                || arguments.getValue(PREFIX_STATUS).isEmpty()) {
            throw new ParseException(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT, ClassPaymentCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_SUBJECT, PREFIX_CLASS, PREFIX_STATUS);

        Index index = ParserUtil.parseIndex(arguments.getPreamble());
        Subject subject = ParserUtil.parseLessonSubject(arguments.getValue(PREFIX_SUBJECT).get());
        LessonSchedule schedule = ParserUtil.parseLessonSchedule(arguments.getValue(PREFIX_CLASS).get());
        PaymentStatus status = ParserUtil.parsePaymentStatus(arguments.getValue(PREFIX_STATUS).get());
        return new ClassPaymentCommand(index, subject, schedule, status);
    }
}
