package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class GuardianContactTest {

    @Test
    public void constructor_optionalFields_validatesSuppliedContacts() {
        assertTrue(GuardianContact.EMPTY.isEmpty());
        assertFalse(new GuardianContact("", "123", "").isEmpty());
        assertThrows(IllegalArgumentException.class, () -> new GuardianContact("@Mary", "", ""));
        assertThrows(IllegalArgumentException.class, () -> new GuardianContact("", "12", ""));
        assertThrows(IllegalArgumentException.class, () -> new GuardianContact("", "", "invalid"));
        assertThrows(NullPointerException.class, () -> new GuardianContact("", null, ""));
    }

    @Test
    public void person_contactUpdates_preserveIdentityAndParticipateInEquality() {
        Person original = new PersonBuilder().build();
        GuardianContact contact = new GuardianContact("Mary Tan", "98765432", "mary@example.com");
        Person updated = original.withGuardianContact(contact);
        assertTrue(original.isSamePerson(updated));
        assertNotEquals(original, updated);
        assertEquals(GuardianContact.EMPTY, original.getGuardianContact());
        assertEquals(updated, new PersonBuilder(updated).build());
        assertEquals(updated.hashCode(), original.withGuardianContact(contact).hashCode());
        assertEquals(contact, updated.withTags(updated.getTags()).getGuardianContact());
        assertEquals(contact, updated.withLessons(updated.getLessons()).getGuardianContact());
    }
}
