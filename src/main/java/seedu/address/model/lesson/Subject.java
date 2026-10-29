package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/** Represents the single subject taught in a lesson. */
public class Subject {

    public static final int MAX_LENGTH = 100;

    public static final String MESSAGE_CONSTRAINTS =
            "Subjects should start with a letter or number and contain only letters, numbers, spaces, "
                    + "and common subject punctuation (+, -, &, apostrophe, period, or parentheses), "
                    + "and should not exceed " + MAX_LENGTH + " characters.";
    public static final String VALIDATION_REGEX = "[\\p{Alnum}][\\p{Alnum} +&'().\\-]*";

    public final String value;

    /** Creates a subject. */
    public Subject(String value) {
        requireNonNull(value);
        String normalizedValue = value.replaceAll(" {2,}", " ");
        checkArgument(isValidSubject(normalizedValue), MESSAGE_CONSTRAINTS);
        this.value = normalizedValue;
    }

    public static boolean isValidSubject(String test) {
        return test.length() <= MAX_LENGTH && test.matches(VALIDATION_REGEX);
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
