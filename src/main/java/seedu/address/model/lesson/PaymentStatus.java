package seedu.address.model.lesson;

import java.util.Locale;

/** Represents the payment status of one student's lesson enrolment. */
public enum PaymentStatus {
    PAID,
    UNPAID;

    public static final String MESSAGE_CONSTRAINTS = "Payment status must be either paid or unpaid.";

    /** Returns whether {@code value} is a supported payment status. */
    public static boolean isValidPaymentStatus(String value) {
        return value != null && (value.equalsIgnoreCase(PAID.toString())
                || value.equalsIgnoreCase(UNPAID.toString()));
    }

    /** Returns the payment status represented by {@code value}. */
    public static PaymentStatus fromString(String value) {
        if (!isValidPaymentStatus(value)) {
            throw new IllegalArgumentException(MESSAGE_CONSTRAINTS);
        }
        return valueOf(value.toUpperCase(Locale.ROOT));
    }

    /** Returns a user-facing representation. */
    public String toDisplayString() {
        return name().charAt(0) + name().substring(1).toLowerCase(Locale.ROOT);
    }

    /** Returns the command representation. */
    @Override
    public String toString() {
        return name().toLowerCase(Locale.ROOT);
    }
}
