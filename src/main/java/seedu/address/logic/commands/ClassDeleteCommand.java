package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_CLASS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_SUBJECT;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;

/** Deletes one class globally by removing it from every enrolled student. */
public class ClassDeleteCommand extends ClassCommand {

    public static final String SUBCOMMAND_WORD = "delete";
    public static final String MESSAGE_USAGE = COMMAND_WORD + " " + SUBCOMMAND_WORD
            + ": Deletes a class from every enrolled student.\n"
            + "Parameters: " + PREFIX_SUBJECT + "SUBJECT " + PREFIX_CLASS + "DAYHHmm-HHmm\n"
            + "Example: " + COMMAND_WORD + " " + SUBCOMMAND_WORD
            + " sub/Math c/Monday1800-1930";
    public static final String MESSAGE_SUCCESS =
            "Deleted class: %1$s, %2$s.%nRemoved enrolments from %3$d %4$s.";
    public static final String MESSAGE_CLASS_NOT_FOUND = "The specified class does not exist.";

    private final Subject subject;
    private final LessonTiming timing;

    /** Creates a command that deletes the specified class globally. */
    public ClassDeleteCommand(Subject subject, LessonTiming timing) {
        this.subject = requireNonNull(subject);
        this.timing = requireNonNull(timing);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> personsToReplace = new ArrayList<>();
        List<Person> replacementPersons = new ArrayList<>();

        for (Person person : List.copyOf(model.getAddressBook().getPersonList())) {
            Set<Lesson> updatedLessons = new HashSet<>(person.getLessons());
            if (updatedLessons.removeIf(lesson -> lesson.isSameClass(subject, timing))) {
                personsToReplace.add(person);
                replacementPersons.add(person.withLessons(updatedLessons));
            }
        }

        if (personsToReplace.isEmpty()) {
            throw new CommandException(MESSAGE_CLASS_NOT_FOUND);
        }

        for (int i = 0; i < personsToReplace.size(); i++) {
            model.setPerson(personsToReplace.get(i), replacementPersons.get(i));
        }

        int affectedStudents = personsToReplace.size();
        String studentLabel = affectedStudents == 1 ? "student" : "students";
        return new CommandResult(String.format(MESSAGE_SUCCESS,
                subject, timing, affectedStudents, studentLabel));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassDeleteCommand otherCommand
                && subject.equals(otherCommand.subject) && timing.equals(otherCommand.timing);
    }
}
