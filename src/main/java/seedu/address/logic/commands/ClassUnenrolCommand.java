package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_ID;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;

/** Unenrols one student from one scheduled class. */
public class ClassUnenrolCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "unenrol";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + SUBCOMMAND_WORD
            + ": Unenrols the indexed student from a class.\n"
            + "Parameters: STUDENT_INDEX " + PREFIX_CLASS_ID + "CLASS_ID\n"
            + "Example: " + COMMAND_WORD + " " + SUBCOMMAND_WORD + " 1 id/C001";
    public static final String MESSAGE_SUCCESS = "Unenrolled %1$s from %2$s, %3$s.";
    public static final String MESSAGE_SUCCESS_BY_ID = "Unenrolled %1$s from %2$s.";
    public static final String MESSAGE_NOT_ENROLLED =
            "This student is not enrolled in the specified class.";

    private final Index personIndex;
    private final ClassId classId;
    private final Subject subject;
    private final LessonSchedule schedule;

    /** Creates an ID-based unenrolment command. */
    public ClassUnenrolCommand(Index personIndex, ClassId classId) {
        this.personIndex = requireNonNull(personIndex);
        this.classId = requireNonNull(classId);
        this.subject = null;
        this.schedule = null;
    }

    /** Creates a command that unenrols the indexed student from the specified class. */
    public ClassUnenrolCommand(Index personIndex, Subject subject, LessonSchedule schedule) {
        this.personIndex = requireNonNull(personIndex);
        this.classId = null;
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
        Set<Enrollment> updatedEnrollments = new LinkedHashSet<>(person.getEnrollments());
        boolean wasRemoved = updatedEnrollments.removeIf(enrollment -> classId != null
                ? classId.equals(enrollment.getClassId())
                : enrollment.getTuitionClass().getSubject().equals(subject)
                        && enrollment.getTuitionClass().getSchedule().equals(schedule));
        if (!wasRemoved) {
            throw new CommandException(MESSAGE_NOT_ENROLLED);
        }

        model.setPerson(person, person.withEnrollments(updatedEnrollments));
        String message = classId != null
                ? String.format(MESSAGE_SUCCESS_BY_ID, person.getName(), classId)
                : String.format(MESSAGE_SUCCESS, person.getName(), subject, schedule);
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassUnenrolCommand otherCommand
                && personIndex.equals(otherCommand.personIndex)
                && java.util.Objects.equals(classId, otherCommand.classId)
                && java.util.Objects.equals(subject, otherCommand.subject)
                && java.util.Objects.equals(schedule, otherCommand.schedule);
    }
}
