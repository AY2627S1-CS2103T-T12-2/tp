package seedu.address.logic;

import static java.util.Objects.requireNonNull;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import seedu.address.logic.parser.Prefix;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/**
 * Container for user visible messages.
 */
public class Messages {

    public static final String MESSAGE_UNKNOWN_COMMAND = "Unknown command.";
    public static final String MESSAGE_INVALID_COMMAND_FORMAT = "Invalid command format!\n%1$s";
    public static final String MESSAGE_INVALID_PERSON_DISPLAYED_INDEX = "The person index provided is invalid.";
    public static final String MESSAGE_PERSONS_LISTED_OVERVIEW = "%1$d person(s) listed!";
    public static final String MESSAGE_DUPLICATE_FIELDS =
                "Multiple values specified for the following single-valued field(s): ";

    /**
     * Returns an error message indicating the duplicate prefixes.
     */
    public static String getErrorMessageForDuplicatePrefixes(Prefix... duplicatePrefixes) {
        assert duplicatePrefixes.length > 0;

        Set<String> duplicateFields =
                Stream.of(duplicatePrefixes).map(Prefix::toString).collect(Collectors.toSet());

        return MESSAGE_DUPLICATE_FIELDS + String.join(" ", duplicateFields);
    }

    /**
     * Formats the {@code person} for display to the user.
     */
    public static String format(Person person) {
        final StringBuilder builder = new StringBuilder();
        builder.append(person.getName())
                .append("; Phone: ")
                .append(person.getPhone())
                .append("; Email: ")
                .append(person.getEmail())
                .append("; Address: ")
                .append(person.getAddress())
                .append("; Tags: ");
        person.getTags().forEach(builder::append);
        if (!person.getGuardianContact().isEmpty()) {
            builder.append("; Guardian: ").append(person.getGuardianContact());
        }
        return builder.toString();
    }

    /**
     * Returns an actionable error message for a candidate class that overlaps existing classes.
     */
    public static String getClassOverlapMessage(Lesson candidate, List<Lesson> conflicts) {
        requireNonNull(candidate);
        requireNonNull(conflicts);
        assert !conflicts.isEmpty();

        String conflictDetails = conflicts.stream()
                .map(Messages::formatLesson)
                .map(formattedLesson -> "- " + formattedLesson)
                .collect(Collectors.joining(System.lineSeparator()));
        return "Cannot add " + formatLesson(candidate) + " because it overlaps with:"
                + System.lineSeparator() + conflictDetails
                + System.lineSeparator() + "Choose a non-overlapping time.";
    }

    private static String formatLesson(Lesson lesson) {
        return lesson.getSubject() + " (" + lesson.getSchedule() + ")";
    }

}
