package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;

/** Unenrols one student from one scheduled class. */
public class ClassUnenrolCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "unenrol";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + SUBCOMMAND_WORD
            + ": Unenrols the indexed student from a class.\n"
            + "Parameters: STUDENT_INDEX " + PREFIX_SUBJECT + "SUBJECT "
            + PREFIX_CLASS + "SCHEDULE\n"
            + "Example: " + COMMAND_WORD + " " + SUBCOMMAND_WORD
            + " 1 sub/Math c/Monday1800-1930";
    public static final String MESSAGE_SUCCESS = "Unenrolled %1$s from %2$s, %3$s.";
    public static final String MESSAGE_NOT_ENROLLED =
            "This student is not enrolled in the specified class.";

    private final Index personIndex;
    private final Subject subject;
    private final LessonSchedule schedule;

    /** Creates a command that unenrols the indexed student from the specified class. */
    public ClassUnenrolCommand(Index personIndex, Subject subject, LessonSchedule schedule) {
        this.personIndex = requireNonNull(personIndex);
        this.subject = requireNonNull(subject);
        this.schedule = requireNonNull(schedule);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> persons = model.getFilteredPersonList();
        if (personIndex.getZeroBased() >= persons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person person = persons.get(personIndex.getZeroBased());
        Set<Lesson> updatedLessons = new HashSet<>(person.getLessons());
        boolean wasRemoved = updatedLessons.removeIf(lesson -> lesson.isSameClass(subject, schedule));
        if (!wasRemoved) {
            throw new CommandException(MESSAGE_NOT_ENROLLED);
        }

        model.setPerson(person, person.withLessons(updatedLessons));
        return new CommandResult(String.format(MESSAGE_SUCCESS, person.getName(), subject, schedule));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassUnenrolCommand otherCommand
                && personIndex.equals(otherCommand.personIndex)
                && subject.equals(otherCommand.subject) && schedule.equals(otherCommand.schedule);
    }
}
