package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Adds a person to the address book.
 */
public class AddCommand extends Command {

    public static final String COMMAND_WORD = "add";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Adds a person to the address book. "
            + "Parameters: "
            + PREFIX_NAME + "NAME "
            + PREFIX_PHONE + "PHONE "
            + PREFIX_EMAIL + "EMAIL "
            + PREFIX_ADDRESS + "ADDRESS "
            + "[" + PREFIX_TAG + "TAG]... [gn/GUARDIAN_NAME] [gp/GUARDIAN_PHONE] [ge/GUARDIAN_EMAIL] "
            + "[" + PREFIX_SUBJECT + "SUBJECT " + PREFIX_COST + "COST "
            + PREFIX_CLASS + "SCHEDULE [mode/online|offline] [link/URL] [location/LOCATION] [notes/NOTES]]\n"
            + "Example: " + COMMAND_WORD + " "
            + PREFIX_NAME + "John Doe "
            + PREFIX_PHONE + "98765432 "
            + PREFIX_EMAIL + "johnd@example.com "
            + PREFIX_ADDRESS + "311, Clementi Ave 2, #02-25 "
            + PREFIX_SUBJECT + "Math " + PREFIX_COST + "30 " + PREFIX_CLASS + "Monday1800-1930";

    public static final String MESSAGE_SUCCESS = "Student Added: %1$s";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";
    private static final Logger logger = LogsCenter.getLogger(AddCommand.class);
    private final Person toAdd;
    private final ClassDetailsUpdate detailsUpdate;

    /**
     * Creates an AddCommand to add the specified {@code Person}
     */
    public AddCommand(Person person) {
        this(person, ClassDetailsUpdate.NONE);
    }

    /** Creates a student with optional inline class fields. */
    public AddCommand(Person person, ClassDetailsUpdate detailsUpdate) {
        this.detailsUpdate = requireNonNull(detailsUpdate);
        requireNonNull(person);
        toAdd = person;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);

        if (model.hasPerson(toAdd) || model.hasNameConflict(toAdd)) {
            logger.info("Student creation rejected: duplicate name.");
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }
        for (Lesson lesson : toAdd.getLessons()) {
            List<Lesson> overlappingClasses = model.findOverlappingClasses(lesson);
            if (!overlappingClasses.isEmpty()) {
                logger.info("Student creation rejected: overlapping class schedule.");
                throw new CommandException(Messages.getClassOverlapMessage(lesson, overlappingClasses));
            }
        }

        Set<Lesson> lessons = new LinkedHashSet<>();
        for (Lesson lesson : toAdd.getLessons()) {
            lessons.add(detailsUpdate.forEnrolment(model, lesson));
        }
        Person student = toAdd.withLessons(lessons);
        model.addPerson(student);
        logger.info("Student created with " + student.getLessons().size() + " class enrolment(s); guardian present: "
                + !student.getGuardianContact().isEmpty() + ".");
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                Messages.format(student, model.isPrivacyModeEnabled())));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddCommand otherAddCommand)) {
            return false;
        }

        return toAdd.equals(otherAddCommand.toAdd) && detailsUpdate.equals(otherAddCommand.detailsUpdate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("toAdd", toAdd)
                .toString();
    }
}
