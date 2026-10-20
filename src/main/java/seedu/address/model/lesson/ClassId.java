package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;

import java.util.Locale;
import java.util.Objects;

/** A stable, human-readable identifier for a tuition class. */
public final class ClassId implements Comparable<ClassId> {

    public static final String MESSAGE_CONSTRAINTS =
            "Class ID must start with C followed by a positive integer, for example C001.";
    private static final String VALIDATION_REGEX = "(?i)C0*[1-9]\\d*";

    public final String value;

    /** Creates and normalizes a class ID. */
    public ClassId(String value) {
        requireNonNull(value);
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        if (!isValidClassId(normalized)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        int sequence = Integer.parseInt(normalized.substring(1));
        this.value = String.format("C%03d", sequence);
    }

    /** Creates the display ID for a persisted sequence number. */
    public static ClassId fromSequence(int sequence) {
        if (sequence < 1) {
            throw new IllegalArgumentException("Class ID sequence must be positive.");
        }
        return new ClassId(String.format("C%03d", sequence));
    }

    /** Returns whether the supplied text is a supported class ID. */
    public static boolean isValidClassId(String value) {
        if (value == null || !value.trim().matches(VALIDATION_REGEX)) {
            return false;
        }
        try {
            return Integer.parseInt(value.trim().substring(1)) > 0;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    public int getSequence() {
        return Integer.parseInt(value.substring(1));
    }

    @Override
    public int compareTo(ClassId other) {
        return Integer.compare(getSequence(), other.getSequence());
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof ClassId otherId && value.equals(otherId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value;
    }
}
