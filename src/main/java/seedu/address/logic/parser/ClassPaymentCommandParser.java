package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ClassPaymentCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.PaymentStatus;

/** Parses arguments for the {@code class payment} command. */
public class ClassPaymentCommandParser implements Parser<ClassPaymentCommand> {

    @Override
    public ClassPaymentCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(
                args, PREFIX_CLASS_ID, PREFIX_SUBJECT, PREFIX_CLASS, PREFIX_STATUS);
        boolean hasId = arguments.getValue(PREFIX_CLASS_ID).isPresent();
        boolean hasSubject = arguments.getValue(PREFIX_SUBJECT).isPresent();
        boolean hasTiming = arguments.getValue(PREFIX_CLASS).isPresent();
        boolean usesOnlyId = hasId && !hasSubject && !hasTiming;
        boolean usesOnlyLegacyDetails = !hasId && hasSubject && hasTiming;
        if (arguments.getPreamble().isEmpty() || usesOnlyId == usesOnlyLegacyDetails
                || arguments.getValue(PREFIX_STATUS).isEmpty()) {
            throw new ParseException(String.format(
                    MESSAGE_INVALID_COMMAND_FORMAT, ClassPaymentCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLASS_ID, PREFIX_SUBJECT, PREFIX_CLASS, PREFIX_STATUS);

        Index index = ParserUtil.parseIndex(arguments.getPreamble());
        PaymentStatus status = ParserUtil.parsePaymentStatus(arguments.getValue(PREFIX_STATUS).get());
        if (usesOnlyId) {
            return new ClassPaymentCommand(index,
                    ParserUtil.parseClassId(arguments.getValue(PREFIX_CLASS_ID).get()), status);
        }
        return new ClassPaymentCommand(index,
                ParserUtil.parseLessonSubject(arguments.getValue(PREFIX_SUBJECT).get()),
                ParserUtil.parseLessonTiming(arguments.getValue(PREFIX_CLASS).get()), status);
    }
}
