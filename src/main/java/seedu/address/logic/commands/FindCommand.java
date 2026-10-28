package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.model.Model;
import seedu.address.model.lesson.LessonStart;
import seedu.address.model.person.LessonContainsKeywordsPredicate;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.ScoredPersonPredicate;

/**
 * Finds and lists all persons in the address book whose name, tag, or lesson matches the search input.
 * Matching is case insensitive.
 */
public class FindCommand extends Command {

    public static final String COMMAND_WORD = "find";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Finds students by name, tag, or lesson.\n"
            + "Formats:\n"
            + COMMAND_WORD + " KEYWORD [MORE_KEYWORDS]...\n"
            + COMMAND_WORD + " t/TAG_KEYWORD [MORE_TAG_KEYWORDS]...\n"
            + COMMAND_WORD + " c/DAY [TIME_HHmm]\n"
            + COMMAND_WORD + " c/TIME_HHmm\n"
            + COMMAND_WORD + " c/DATE [TIME_HHmm]\n"
            + "Examples: " + COMMAND_WORD + " ali; " + COMMAND_WORD + " t/pri; "
            + COMMAND_WORD + " c/Monday 1800; " + COMMAND_WORD + " c/1800; "
            + COMMAND_WORD + " c/2026-10-20";

    public static final String MESSAGE_EMPTY_QUERY =
            "Find requires a search value. Use: find KEYWORD, find t/TAG, or find c/LESSON.";
    public static final String MESSAGE_PREFIX_WITH_NAME =
            "Find prefixes must come immediately after 'find'. "
                    + "Use either find KEYWORD, find t/TAG, or find c/LESSON.";
    public static final String MESSAGE_MULTIPLE_SEARCH_MODES =
            "Find can search only one category at a time. Choose name keywords, t/TAG keywords, or c/LESSON keywords.";
    public static final String MESSAGE_DUPLICATE_TAG_PREFIX =
            "Find tag search accepts only one t/ prefix. Put multiple tag keywords after the same t/.";
    public static final String MESSAGE_DUPLICATE_CLASS_PREFIX =
            "Find lesson search accepts only one c/ prefix. Put the schedule search values after the same c/.";
    public static final String MESSAGE_EMPTY_TAG =
            "Find tag search requires at least one tag keyword after t/. Example: find t/priority.";
    public static final String MESSAGE_EMPTY_LESSON =
            "Enter a day, date, time, or a supported combination after c/. Example: find c/Monday 1800.";
    public static final String MESSAGE_INVALID_LESSON =
            "Lesson search should use a weekday (e.g. Mon), ISO date (e.g. 2026-10-20), "
                    + "time 0000-2359, or a supported combination.";

    private final Predicate<Person> predicate;

    public FindCommand(NameContainsKeywordsPredicate predicate) {
        this((Predicate<Person>) predicate);
    }

    /** Creates a command that finds people using the given predicate. */
    public FindCommand(Predicate<Person> predicate) {
        this.predicate = requireNonNull(predicate);
    }

    /** Creates a command that finds people enrolled in lessons starting at the given time. */
    public FindCommand(LessonStart lessonStart) {
        this(new LessonContainsKeywordsPredicate(requireNonNull(lessonStart).toString()));
    }

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        if (predicate instanceof ScoredPersonPredicate scoredPredicate) {
            model.updateFilteredPersonList(predicate, getScoreComparator(model, scoredPredicate));
        } else {
            model.updateFilteredPersonList(predicate);
        }
        return new CommandResult(
                String.format(Messages.MESSAGE_PERSONS_LISTED_OVERVIEW, model.getFilteredPersonList().size()));
    }

    private Comparator<Person> getScoreComparator(Model model, ScoredPersonPredicate scoredPredicate) {
        List<Person> currentOrder = new ArrayList<>(model.getFilteredPersonList());
        for (Person person : model.getAddressBook().getPersonList()) {
            if (!currentOrder.contains(person)) {
                currentOrder.add(person);
            }
        }
        return Comparator.<Person>comparingInt(scoredPredicate::score).reversed()
                .thenComparingInt(currentOrder::indexOf);
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

        return predicate.equals(otherFindCommand.predicate);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("predicate", predicate).toString();
    }
}
