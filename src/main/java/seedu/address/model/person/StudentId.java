package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** An immutable, stable identifier for a student. */
public final class StudentId {

    public static final String MESSAGE_CONSTRAINTS =
            "Student ID must start with S- followed by a UUID.";
    private static final String VALIDATION_REGEX =
            "(?i)S-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    public final String value;

    /** Creates a student ID from its persisted representation. */
    public StudentId(String value) {
        requireNonNull(value);
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        checkArgument(isValidStudentId(normalized), MESSAGE_CONSTRAINTS);
        this.value = normalized;
    }

    /** Generates a new stable student ID. */
    public static StudentId generate() {
        return new StudentId("S-" + UUID.randomUUID());
    }

    public static boolean isValidStudentId(String value) {
        return value != null && value.trim().matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof StudentId otherId && value.equals(otherId.value);
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
