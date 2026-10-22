package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_ID;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;

/** Deletes one class globally by removing it from every enrolled student. */
public class ClassDeleteCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "delete";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + SUBCOMMAND_WORD
            + ": Deletes a class from every enrolled student.\n"
            + "Parameters: " + PREFIX_CLASS_ID + "CLASS_ID\n"
            + "Example: " + COMMAND_WORD + " " + SUBCOMMAND_WORD + " id/C001";
    public static final String MESSAGE_SUCCESS =
            "Deleted class: %1$s, %2$s.%nRemoved enrolments from %3$d %4$s.";
    public static final String MESSAGE_CLASS_NOT_FOUND = "The specified class does not exist.";

    private final ClassId classId;
    private final Subject subject;
    private final LessonSchedule schedule;

    /** Creates an ID-based class deletion command. */
    public ClassDeleteCommand(ClassId classId) {
        this.classId = requireNonNull(classId);
        this.subject = null;
        this.schedule = null;
    }

    /** Creates a command that deletes the specified class globally. */
    public ClassDeleteCommand(Subject subject, LessonSchedule schedule) {
        this.classId = null;
        this.subject = requireNonNull(subject);
        this.schedule = requireNonNull(schedule);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        TuitionClass target = classId != null && model.hasTuitionClass(classId)
                ? model.getTuitionClass(classId)
                : model.getTuitionClassList().stream()
                        .filter(item -> subject != null && item.getSubject().equals(subject)
                                && item.getSchedule().equals(schedule))
                        .findFirst().orElse(null);
        if (target == null) {
            throw new CommandException(MESSAGE_CLASS_NOT_FOUND);
        }

        int affectedStudents = (int) model.getAddressBook().getPersonList().stream()
                .filter(person -> person.getEnrollments().stream()
                        .anyMatch(enrollment -> target.getClassId().equals(enrollment.getClassId())))
                .count();
        model.deleteTuitionClass(target);
        String studentLabel = affectedStudents == 1 ? "student" : "students";
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                classId == null ? subject : target.getSubject(), target.getSchedule(), affectedStudents, studentLabel));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassDeleteCommand otherCommand
                && java.util.Objects.equals(classId, otherCommand.classId)
                && java.util.Objects.equals(subject, otherCommand.subject)
                && java.util.Objects.equals(schedule, otherCommand.schedule);
    }
}
