package seedu.address.model.person;

import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.stream.Collectors;
import java.util.stream.Stream;

/** Optional contact details for one student's parent or guardian. Empty fields mean not provided. */
public record GuardianContact(String name, String phone, String email) {

    public static final GuardianContact EMPTY = new GuardianContact("", "", "");
    public static final String MESSAGE_NAME_CONSTRAINTS = "Guardian names must start with a letter or digit "
            + "and contain only letters, digits, spaces, hyphens and apostrophes.";
    private static final String NAME_REGEX = "[\\p{Alnum}][\\p{Alnum} '\\u2019-]*";

    /** Creates a contact; guardian names also accept common name punctuation. */
    public GuardianContact {
        requireAllNonNull(name, phone, email);
        checkArgument(name.isEmpty() || name.matches(NAME_REGEX), MESSAGE_NAME_CONSTRAINTS);
        checkArgument(phone.isEmpty() || Phone.isValidPhone(phone), "Guardian phone: " + Phone.MESSAGE_CONSTRAINTS);
        checkArgument(email.isEmpty() || Email.isValidEmail(email), "Guardian email: " + Email.MESSAGE_CONSTRAINTS);
    }

    /** Returns whether no guardian details were supplied. */
    public boolean isEmpty() {
        return name.isEmpty() && phone.isEmpty() && email.isEmpty();
    }

    @Override
    public String toString() {
        return Stream.of(name, phone, email).filter(value -> !value.isEmpty()).collect(Collectors.joining(" | "));
    }
}
