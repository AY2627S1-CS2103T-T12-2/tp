package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.lesson.LessonStart;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;

/**
 * Finds and lists all persons in the address book whose name contains any of the argument keywords.
 * Keyword matching is case insensitive.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds persons by name, tag, or lesson start.\n"
            + "Parameters: KEYWORD [MORE_KEYWORDS]... | t/TAG | c/DAYHHmm\n"
            + "Examples: " + COMMAND_WORD + " alice bob; " + COMMAND_WORD + " t/priority; "
            + COMMAND_WORD + " c/Monday1800";

    private final Predicate<Person> predicate;
    private final LessonStart lessonStart;

    public FindCommand(NameContainsKeywordsPredicate predicate) {
        this((Predicate<Person>) predicate);
    }

    /** Creates a command that finds people using the given predicate. */
    public FindCommand(Predicate<Person> predicate) {
        this.predicate = predicate;
        this.lessonStart = null;
    }

    /** Creates a command that finds people enrolled in lessons starting at the given time. */
    public FindCommand(LessonStart lessonStart) {
        this.predicate = null;
        this.lessonStart = lessonStart;
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        if (lessonStart == null) {
            model.updateFilteredPersonList(predicate);
        } else {
            model.updateFilteredPersonList(person -> person.getLessons().stream()
                    .anyMatch(lesson -> lessonStart.matches(lesson.getTiming())));
        }
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof FindCommand otherFindCommand)) {
            return false;
        }

        return java.util.Objects.equals(predicate, otherFindCommand.predicate)
                && java.util.Objects.equals(lessonStart, otherFindCommand.lessonStart);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this).add("predicate", predicate);
        if (lessonStart != null) {
            builder.add("lessonStart", lessonStart);
        }
        return builder.toString();
    }
}
