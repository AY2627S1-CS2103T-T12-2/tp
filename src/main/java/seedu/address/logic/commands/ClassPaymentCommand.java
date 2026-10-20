package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS_ID;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;

/** Updates the payment status of one student's lesson enrolment. */
public class ClassPaymentCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "payment";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + SUBCOMMAND_WORD
            + ": Updates the payment status of the indexed student's class enrolment.\n"
            + "Parameters: STUDENT_INDEX " + PREFIX_CLASS_ID + "CLASS_ID "
            + PREFIX_STATUS + "paid|unpaid\n"
            + "Example: " + COMMAND_WORD + " " + SUBCOMMAND_WORD
            + " 1 id/C001 status/paid";
    public static final String MESSAGE_SUCCESS =
            "Payment for %1$s in %2$s (%3$s) marked as %4$s.";
    public static final String MESSAGE_ALREADY_MARKED =
            "Payment for %1$s in %2$s (%3$s) is already marked as %4$s.";
    public static final String MESSAGE_NOT_ENROLLED =
            "This student is not enrolled in %1$s (%2$s). No payment status was changed.";
    public static final String MESSAGE_SUCCESS_BY_ID =
            "Payment for %1$s in %2$s marked as %3$s.";
    public static final String MESSAGE_ALREADY_MARKED_BY_ID =
            "Payment for %1$s in %2$s is already marked as %3$s.";
    public static final String MESSAGE_NOT_ENROLLED_BY_ID =
            "This student is not enrolled in %1$s. No payment status was changed.";

    private final Index personIndex;
    private final ClassId classId;
    private final Subject subject;
    private final LessonTiming timing;
    private final PaymentStatus paymentStatus;

    /** Creates an ID-based payment command. */
    public ClassPaymentCommand(Index personIndex, ClassId classId, PaymentStatus paymentStatus) {
        this.personIndex = requireNonNull(personIndex);
        this.classId = requireNonNull(classId);
        this.subject = null;
        this.timing = null;
        this.paymentStatus = requireNonNull(paymentStatus);
    }

    /** Creates a command that updates the selected enrolment's payment status. */
    public ClassPaymentCommand(Index personIndex, Subject subject, LessonTiming timing,
            PaymentStatus paymentStatus) {
        this.personIndex = requireNonNull(personIndex);
        this.classId = null;
        this.subject = requireNonNull(subject);
        this.timing = requireNonNull(timing);
        this.paymentStatus = requireNonNull(paymentStatus);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> persons = model.getFilteredPersonList();
        if (personIndex.getZeroBased() >= persons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person person = persons.get(personIndex.getZeroBased());
        Lesson matchingLesson = person.getLessons().stream()
                .filter(lesson -> classId != null
                        ? classId.equals(lesson.getClassId()) : lesson.isSameClass(subject, timing))
                .findFirst()
                .orElseThrow(() -> new CommandException(classId != null
                        ? String.format(MESSAGE_NOT_ENROLLED_BY_ID, classId)
                        : String.format(MESSAGE_NOT_ENROLLED, subject, timing)));

        String displayStatus = paymentStatus.toString();
        if (matchingLesson.getPaymentStatus() == paymentStatus) {
            String message = classId != null
                    ? String.format(MESSAGE_ALREADY_MARKED_BY_ID, person.getName(), classId, displayStatus)
                    : String.format(MESSAGE_ALREADY_MARKED, person.getName(), subject, timing, displayStatus);
            return new CommandResult(message);
        }

        Set<Lesson> updatedLessons = new HashSet<>(person.getLessons());
        updatedLessons.remove(matchingLesson);
        updatedLessons.add(matchingLesson.withPaymentStatus(paymentStatus));
        model.setPerson(person, person.withLessons(updatedLessons));
        String message = classId != null
                ? String.format(MESSAGE_SUCCESS_BY_ID, person.getName(), classId, displayStatus)
                : String.format(MESSAGE_SUCCESS, person.getName(), subject, timing, displayStatus);
        return new CommandResult(message);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassPaymentCommand otherCommand
                && personIndex.equals(otherCommand.personIndex)
                && java.util.Objects.equals(classId, otherCommand.classId)
                && java.util.Objects.equals(subject, otherCommand.subject)
                && java.util.Objects.equals(timing, otherCommand.timing)
                && paymentStatus == otherCommand.paymentStatus;
    }
}
