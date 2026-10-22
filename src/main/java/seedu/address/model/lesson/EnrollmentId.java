package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/** An immutable, stable identifier for a student-class enrolment. */
public final class EnrollmentId {

    public static final String MESSAGE_CONSTRAINTS =
            "Enrollment ID must start with E- followed by a UUID.";
    private static final String VALIDATION_REGEX =
            "(?i)E-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}";

    public final String value;

    /** Creates an enrollment ID from its persisted representation. */
    public EnrollmentId(String value) {
        requireNonNull(value);
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        checkArgument(isValidEnrollmentId(normalized), MESSAGE_CONSTRAINTS);
        this.value = normalized;
    }

    /** Generates a new stable enrollment ID. */
    public static EnrollmentId generate() {
        return new EnrollmentId("E-" + UUID.randomUUID());
    }

    public static boolean isValidEnrollmentId(String value) {
        return value != null && value.trim().matches(VALIDATION_REGEX);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof EnrollmentId otherId && value.equals(otherId.value);
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
