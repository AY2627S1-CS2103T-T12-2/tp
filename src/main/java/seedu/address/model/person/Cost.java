package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

/**
 * Represents a Student's hourly rate in the address book.
 * Guarantees: immutable; is valid as declared in {@link #isValidCost(String)}
 */
public class Cost {


    public static final String MESSAGE_CONSTRAINTS =
            "Cost should be a positive integer";
    public static final String VALIDATION_REGEX = "[1-9][0-9]*";
    public final String value;

    /**
     * Constructs a {@code Cost}.
     *
     * @param cost A valid cost.
     */
    public Cost(String cost) {
        requireNonNull(cost);
        checkArgument(isValidCost(cost), MESSAGE_CONSTRAINTS);
        value = cost;
    }

    /**
     * Returns true if a given string is a valid cost.
     */
    public static boolean isValidCost(String test) {
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

        // instanceof handles nulls
        if (!(other instanceof Cost otherCost)) {
            return false;
        }

        return value.equals(otherCost.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

}
