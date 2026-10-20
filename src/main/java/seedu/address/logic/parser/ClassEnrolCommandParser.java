package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.List;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ClassEnrolCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;

/** Parses arguments for the {@code class enrol} command. */
public class ClassEnrolCommandParser implements Parser<ClassEnrolCommand> {

    @Override
    public ClassEnrolCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS);
        if (arguments.getPreamble().isEmpty() || arguments.getValue(PREFIX_SUBJECT).isEmpty()
                || arguments.getValue(PREFIX_CLASS).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassEnrolCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS);

        List<Index> indexes = ParserUtil.parseIndexes(arguments.getPreamble());
        Subject subject = ParserUtil.parseLessonSubject(arguments.getValue(PREFIX_SUBJECT).get());
        LessonTiming timing = ParserUtil.parseLessonTiming(arguments.getValue(PREFIX_CLASS).get());
        if (arguments.getValue(PREFIX_COST).isEmpty()) {
            return new ClassEnrolCommand(indexes, subject, timing);
        }
        if (indexes.size() > 1) {
            throw new ParseException(ClassEnrolCommand.MESSAGE_BATCH_COST_NOT_ALLOWED);
        }
        Lesson lesson = new Lesson(subject,
                ParserUtil.parseLessonCost(arguments.getValue(PREFIX_COST).get()), timing);
        return new ClassEnrolCommand(indexes.get(0), lesson);
    }
}
