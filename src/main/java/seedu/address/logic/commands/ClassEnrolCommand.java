package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_COST;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.commons.core.index.Index;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/** Enrols an existing student in a recurring weekly class. */
public class ClassEnrolCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "enrol";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + SUBCOMMAND_WORD
            + ": Enrols the indexed student in a class.\n"
            + "Parameters: STUDENT_INDEX " + PREFIX_SUBJECT + "SUBJECT " + PREFIX_COST + "COST "
            + PREFIX_CLASS + "DAYHHmm-HHmm\n"
            + "Example: " + COMMAND_WORD + " " + SUBCOMMAND_WORD
            + " 1 sub/Math cost/30 c/Monday1800-1930";
    public static final String MESSAGE_SUCCESS = "Enrolled %1$s in class: %2$s";
    public static final String MESSAGE_DUPLICATE_CLASS = "This student is already enrolled in the specified class.";
    public static final String MESSAGE_CLASS_COST_CONFLICT =
            "This class already exists with a different cost. Students in the same class must have the same cost.";

    private final Index personIndex;
    private final Lesson lesson;

    /** Creates a command that enrols the indexed student in {@code lesson}. */
    public ClassEnrolCommand(Index personIndex, Lesson lesson) {
        this.personIndex = requireNonNull(personIndex);
        this.lesson = requireNonNull(lesson);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> persons = model.getFilteredPersonList();
        if (personIndex.getZeroBased() >= persons.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person person = persons.get(personIndex.getZeroBased());
        if (model.hasClassCostConflict(lesson)) {
            throw new CommandException(MESSAGE_CLASS_COST_CONFLICT);
        }
        if (person.getLessons().stream().anyMatch(existing -> existing.isSameClass(lesson))) {
            throw new CommandException(MESSAGE_DUPLICATE_CLASS);
        }
        List<Lesson> overlappingClasses = model.findOverlappingClasses(lesson);
        if (!overlappingClasses.isEmpty()) {
            throw new CommandException(Messages.getClassOverlapMessage(lesson, overlappingClasses));
        }
        Set<Lesson> updatedLessons = new HashSet<>(person.getLessons());
        updatedLessons.add(lesson);
        model.setPerson(person, person.withLessons(updatedLessons));
        return new CommandResult(String.format(MESSAGE_SUCCESS, person.getName(), lesson));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassEnrolCommand otherCommand
                && personIndex.equals(otherCommand.personIndex) && lesson.equals(otherCommand.lesson);
    }
}
