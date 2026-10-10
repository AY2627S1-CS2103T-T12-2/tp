package seedu.address.logic.parser;

import static seedu.address.logic.Messages.MESSAGE_INVALID_COMMAND_FORMAT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Parses input arguments and creates a new AddCommand object
 */
public class AddCommandParser implements Parser<AddCommand> {

    /**
     * Parses the given {@code String} of arguments in the context of the AddCommand
     * and returns an AddCommand object for execution.
     * @throws ParseException if the user input does not conform to the expected format
     */
    public AddCommand parse(String args) throws ParseException {
        ArgumentMultimap argMultimap =
                ArgumentTokenizer.tokenize(args, PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS, PREFIX_TAG,
                        PREFIX_SUBJECT, PREFIX_COST, PREFIX_CLASS);

        if (!arePrefixesPresent(argMultimap, PREFIX_NAME, PREFIX_ADDRESS, PREFIX_PHONE, PREFIX_EMAIL)
                || !argMultimap.getPreamble().isEmpty()) {
            throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
        }

        argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_NAME, PREFIX_PHONE, PREFIX_EMAIL, PREFIX_ADDRESS,
                PREFIX_COST, PREFIX_CLASS);
        boolean hasSubject = argMultimap.getValue(PREFIX_SUBJECT).isPresent();
        boolean hasCost = argMultimap.getValue(PREFIX_COST).isPresent();
        boolean hasClass = argMultimap.getValue(PREFIX_CLASS).isPresent();
        boolean usesLessonModel = hasClass && argMultimap.getValue(PREFIX_CLASS).get().contains("-");
        Name name = ParserUtil.parseName(argMultimap.getValue(PREFIX_NAME).get());
        Phone phone = ParserUtil.parsePhone(argMultimap.getValue(PREFIX_PHONE).get());
        Email email = ParserUtil.parseEmail(argMultimap.getValue(PREFIX_EMAIL).get());
        Address address = ParserUtil.parseAddress(argMultimap.getValue(PREFIX_ADDRESS).get());
        Set<Tag> tagList = ParserUtil.parseTags(argMultimap.getAllValues(PREFIX_TAG));
        Person person;
        if (usesLessonModel) {
            if (!(hasSubject && hasCost)) {
                throw new ParseException(String.format(MESSAGE_INVALID_COMMAND_FORMAT, AddCommand.MESSAGE_USAGE));
            }
            argMultimap.verifyNoDuplicatePrefixesFor(PREFIX_SUBJECT);
            Set<Lesson> lessons = new java.util.LinkedHashSet<>();
            lessons.add(new Lesson(ParserUtil.parseLessonSubject(argMultimap.getValue(PREFIX_SUBJECT).get()),
                    ParserUtil.parseLessonCost(argMultimap.getValue(PREFIX_COST).get()),
                    ParserUtil.parseLessonTiming(argMultimap.getValue(PREFIX_CLASS).get())));
            person = new Person(name, phone, email, address, tagList, lessons);
        } else {
            Set<seedu.address.model.person.Subject> subjects =
                    ParserUtil.parseSubjects(argMultimap.getAllValues(PREFIX_SUBJECT));
            Optional<seedu.address.model.person.Cost> cost = hasCost
                    ? Optional.of(ParserUtil.parseCost(argMultimap.getValue(PREFIX_COST).get()))
                    : Optional.empty();
            Optional<seedu.address.model.person.Lesson> lesson = hasClass
                    ? Optional.of(ParserUtil.parseLesson(argMultimap.getValue(PREFIX_CLASS).get()))
                    : Optional.empty();
            person = new Person(name, phone, email, address, tagList, subjects, cost, lesson);
        }

        return new AddCommand(person);
    }

    /**
     * Returns true if none of the prefixes contains empty {@code Optional} values in the given
     * {@code ArgumentMultimap}.
     */
    private static boolean arePrefixesPresent(ArgumentMultimap argumentMultimap, Prefix... prefixes) {
        return Stream.of(prefixes).allMatch(prefix -> argumentMultimap.getValue(prefix).isPresent());
    }

}
