package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.commands.AddClassCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.Lesson;

/** Parses the addclass command. */
public class AddClassCommandParser implements Parser<AddClassCommand> {

    @Override
    public AddClassCommand parse(String args) throws ParseException {
        ArgumentMultimap arguments = ArgumentTokenizer.tokenize(args, PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS);
        if (arguments.getPreamble().isEmpty() || arguments.getValue(PREFIX_SUBJECT).isEmpty()
                || arguments.getValue(PREFIX_COST).isEmpty() || arguments.getValue(PREFIX_CLASS).isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddClassCommand.MESSAGE_USAGE));
        }
        arguments.verifyNoDuplicatePrefixesFor(PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS);
        Index index = ParserUtil.parseIndex(arguments.getPreamble());
        Lesson lesson = new Lesson(ParserUtil.parseLessonSubject(arguments.getValue(PREFIX_SUBJECT).get()),
                ParserUtil.parseLessonCost(arguments.getValue(PREFIX_COST).get()),
                ParserUtil.parseLessonTiming(arguments.getValue(PREFIX_CLASS).get()));
        return new AddClassCommand(index, lesson);
    }
}
