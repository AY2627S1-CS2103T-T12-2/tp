package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_STATUS;
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
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;

/** Updates the payment status of one student's lesson enrolment. */
public class ClassPaymentCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "payment";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + SUBCOMMAND_WORD
            + ": Updates the payment status of the indexed student's class enrolment.\n"
            + "Parameters: STUDENT_INDEX " + PREFIX_SUBJECT + "SUBJECT "
            + PREFIX_CLASS + "SCHEDULE " + PREFIX_STATUS + "paid|unpaid\n"
            + "Example: " + COMMAND_WORD + " " + SUBCOMMAND_WORD
            + " 1 sub/Math c/Monday1800-1930 status/paid";
    public static final String MESSAGE_SUCCESS =
            "Payment for %1$s in %2$s (%3$s) marked as %4$s.";
    public static final String MESSAGE_ALREADY_MARKED =
            "Payment for %1$s in %2$s (%3$s) is already marked as %4$s.";
    public static final String MESSAGE_NOT_ENROLLED =
            "This student is not enrolled in %1$s (%2$s). No payment status was changed.";

    private final Index personIndex;
    private final Subject subject;
    private final LessonSchedule schedule;
    private final PaymentStatus paymentStatus;

    /** Creates a command that updates the selected enrolment's payment status. */
    public ClassPaymentCommand(Index personIndex, Subject subject, LessonSchedule schedule,
            PaymentStatus paymentStatus) {
        this.personIndex = requireNonNull(personIndex);
        this.subject = requireNonNull(subject);
        this.schedule = requireNonNull(schedule);
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
                .filter(lesson -> lesson.isSameClass(subject, schedule))
                .findFirst()
                .orElseThrow(() -> new CommandException(
                        String.format(MESSAGE_NOT_ENROLLED, subject, schedule)));

        String displayStatus = paymentStatus.toString();
        if (matchingLesson.getPaymentStatus() == paymentStatus) {
            return new CommandResult(String.format(MESSAGE_ALREADY_MARKED,
                    person.getName(), subject, schedule, displayStatus));
        }

        Set<Lesson> updatedLessons = new HashSet<>(person.getLessons());
        updatedLessons.remove(matchingLesson);
        updatedLessons.add(matchingLesson.withPaymentStatus(paymentStatus));
        model.setPerson(person, person.withLessons(updatedLessons));
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                person.getName(), subject, schedule, displayStatus));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassPaymentCommand otherCommand
                && personIndex.equals(otherCommand.personIndex)
                && subject.equals(otherCommand.subject)
                && schedule.equals(otherCommand.schedule)
                && paymentStatus == otherCommand.paymentStatus;
    }
}
