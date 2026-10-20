package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LINK;
import static seedu.address.logic.parser.CliSyntax.PREFIX_LOCATION;
import static seedu.address.logic.parser.CliSyntax.PREFIX_MODE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NOTES;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.ClassEnrolCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.Lesson;

/** Parses arguments for the {@code class enrol} command. */
public class ClassEnrolCommandParser implements Parser<ClassEnrolCommand> {

    @Override
    public ClassEnrolCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS,
                PREFIX_MODE, PREFIX_LINK, PREFIX_LOCATION, PREFIX_NOTES);
        if (arguments.getPreamble().isEmpty() || arguments.getValue(PREFIX_SUBJECT).isEmpty()
                || arguments.getValue(PREFIX_COST).isEmpty() || arguments.getValue(PREFIX_CLASS).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, ClassEnrolCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS,
                PREFIX_MODE, PREFIX_LINK, PREFIX_LOCATION, PREFIX_NOTES);

        Index index = ParserUtil.parseIndex(arguments.getPreamble());
        Lesson lesson = new Lesson(ParserUtil.parseLessonSubject(arguments.getValue(PREFIX_SUBJECT).get()),
                ParserUtil.parseLessonCost(arguments.getValue(PREFIX_COST).get()),
                ParserUtil.parseLessonTiming(arguments.getValue(PREFIX_CLASS).get()));
        return new ClassEnrolCommand(index, lesson, ParserUtil.parseClassDetailsUpdate(arguments));
    }
}
