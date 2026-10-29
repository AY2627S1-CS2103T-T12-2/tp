package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ClassEnrolCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.Subject;

/** Parses arguments for the {@code class enrol} command. */
public class ClassEnrolCommandParser implements Parser<ClassEnrolCommand> {

    public static final String MESSAGE_ID_REQUIRES_ONE_STUDENT =
            "Class ID enrolment accepts exactly one student index. To enrol multiple students, use: "
                    + "class enrol STUDENT_INDEX [STUDENT_INDEX]... sub/SUBJECT c/SCHEDULE";

    @Override
    public ClassEnrolCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args,
                PREFIX_CLASS_ID, PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS);
        boolean hasId = arguments.getValue(PREFIX_CLASS_ID).isPresent();
        boolean hasSubject = arguments.getValue(PREFIX_SUBJECT).isPresent();
        boolean hasCost = arguments.getValue(PREFIX_COST).isPresent();
        boolean hasTiming = arguments.getValue(PREFIX_CLASS).isPresent();
        boolean usesOnlyId = hasId && !hasSubject && !hasCost && !hasTiming;
        boolean usesExistingClassDetails = !hasId && hasSubject && !hasCost && hasTiming;
        boolean usesLegacyDetailsWithCost = !hasId && hasSubject && hasCost && hasTiming;
        if (arguments.getPreamble().isEmpty()
                || !(usesOnlyId || usesExistingClassDetails || usesLegacyDetailsWithCost)) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassEnrolCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_CLASS_ID, PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS);

        if (usesOnlyId) {
            List<Index> indexes = ParserUtil.parseIndexes(arguments.getPreamble());
            if (indexes.size() != 1) {
                throw new ParseException(MESSAGE_ID_REQUIRES_ONE_STUDENT);
            }
            return new ClassEnrolCommand(indexes.get(0),
                    ParserUtil.parseClassId(arguments.getValue(PREFIX_CLASS_ID).get()));
        }
        List<Index> indexes = ParserUtil.parseIndexes(arguments.getPreamble());
        Subject subject = ParserUtil.parseLessonSubject(arguments.getValue(PREFIX_SUBJECT).get());
        LessonSchedule schedule = ParserUtil.parseLessonSchedule(arguments.getValue(PREFIX_CLASS).get());
        if (usesExistingClassDetails) {
            return new ClassEnrolCommand(indexes, subject, schedule);
        }
        if (indexes.size() > 1) {
            throw new ParseException(ClassEnrolCommand.MESSAGE_BATCH_COST_NOT_ALLOWED);
        }
        Lesson lesson = new Lesson(subject,
                ParserUtil.parseLessonCost(arguments.getValue(PREFIX_COST).get()), schedule);
        return new ClassEnrolCommand(indexes.get(0), lesson);
    }
}
