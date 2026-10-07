package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a student's lesson timing in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidLesson(String)}
 */
public class Lesson {


    public static final String MESSAGE_CONSTRAINTS =
            "Lesson timing should be a weekday followed by a four-digit "
            + "24-hour time, for example Monday1800";

    public static final String VALIDATION_REGEX =
            "(?i)(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)"
            + "([01][0-9]|2[0-3])[0-5][0-9]";

    public final String value;

    /**
     * Constructs a {@code Lesson}.
     *
     * @param lesson A valid lesson.
     */
    public Lesson(String lesson) {
        requireNonNull(lesson);
        checkArgument(isValidLesson(lesson), MESSAGE_CONSTRAINTS);
        value = lesson;
    }

    /**
     * Returns true if a given string is a valid lesson.
     */
    public static boolean isValidLesson(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        if (!(other instanceof Lesson otherLesson)) {
            return false;
        }

        return value.equals(otherLesson.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
