package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.math.BigDecimal;

/** Represents the non-negative per-student, per-session cost of a lesson. */
public class Cost {

    public static final String MESSAGE_CONSTRAINTS =
            "Cost should be a non-negative number with at most two decimal places.";
    public static final String VALIDATION_REGEX = "(?:0|[1-9]\\d*)(?:\\.\\d{1,2})?";

    public final BigDecimal value;

    /** Creates a cost from its decimal representation. */
    public Cost(String value) {
        requireNonNull(value);
        checkArgument(isValidCost(value), MESSAGE_CONSTRAINTS);
        this.value = new BigDecimal(value).stripTrailingZeros();
    }

    public static boolean isValidCost(String test) {
        return test.matches(VALIDATION_REGEX);
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof Cost otherCost && value.compareTo(otherCost.value) == 0;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}
