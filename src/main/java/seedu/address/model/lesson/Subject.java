package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/** Represents the single subject taught in a lesson. */
public class Subject {

    public static final String MESSAGE_CONSTRAINTS =
            "Subjects should contain letters, numbers and spaces, and should not be blank.";
    public static final String VALIDATION_REGEX = "[\\p{Alnum}][\\p{Alnum} ]*";

    public final String value;

    /** Creates a subject. */
    public Subject(String value) {
        requireNonNull(value);
        checkArgument(isValidSubject(value), MESSAGE_CONSTRAINTS);
        this.value = value;
    }

    public static boolean isValidSubject(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Subject otherSubject && value.equalsIgnoreCase(otherSubject.value);
    }

    @Override
    public int hashCode() {
        return value.toLowerCase(java.util.Locale.ROOT).hashCode();
    }
}
