package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ClassEnrolCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.Lesson;

/** Parses arguments for the {@code class enrol} command. */
public class ClassEnrolCommandParser implements Parser<ClassEnrolCommand> {

    @Override
    public ClassEnrolCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args,
                PREFIX_CLASS_ID, PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS);
        boolean hasId = arguments.getValue(PREFIX_CLASS_ID).isPresent();
        boolean hasSubject = arguments.getValue(PREFIX_SUBJECT).isPresent();
        boolean hasCost = arguments.getValue(PREFIX_COST).isPresent();
        boolean hasTiming = arguments.getValue(PREFIX_CLASS).isPresent();
        boolean usesOnlyId = hasId && !hasSubject && !hasCost && !hasTiming;
        boolean usesOnlyLegacyDetails = !hasId && hasSubject && hasCost && hasTiming;
        if (arguments.getPreamble().isEmpty() || usesOnlyId == usesOnlyLegacyDetails) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassEnrolCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLASS_ID, PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS);

        Index index = ParserUtil.parseIndex(arguments.getPreamble());
        if (usesOnlyId) {
            return new ClassEnrolCommand(index, ParserUtil.parseClassId(arguments.getValue(PREFIX_CLASS_ID).get()));
        }
        Lesson lesson = new Lesson(ParserUtil.parseLessonSubject(arguments.getValue(PREFIX_SUBJECT).get()),
                ParserUtil.parseLessonCost(arguments.getValue(PREFIX_COST).get()),
                ParserUtil.parseLessonSchedule(arguments.getValue(PREFIX_CLASS).get()));
        return new ClassEnrolCommand(index, lesson);
    }
}
