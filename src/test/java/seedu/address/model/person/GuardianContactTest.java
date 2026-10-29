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
    public void constructor_namePunctuation_acceptsCommonNamesButRejectsInvalidNames() {
        for (String name : java.util.List.of("Mary-Anne O'Connor", "O\u2019Connor", "Anne-Marie")) {
            assertEquals(name, new GuardianContact(name, "", "").name());
        }
        for (String name : java.util.List.of(" ", "'", "-", "@Mary", "Mary\nTan", "Mary_Tan")) {
            assertThrows(IllegalArgumentException.class, () -> new GuardianContact(name, "", ""));
        }
        // Guardian validation is independent of the student-name rule.
        assertFalse(Name.isValidName("Mary-Anne O'Connor"));
    }

    @Test
    public void constructor_contactLengthBoundaries_acceptsMaximumAndRejectsExcessiveValues() {
        assertEquals(GuardianContact.MAX_NAME_LENGTH - 1,
                new GuardianContact("A".repeat(GuardianContact.MAX_NAME_LENGTH - 1), "", "").name().length());
        assertEquals(GuardianContact.MAX_NAME_LENGTH,
                new GuardianContact("A".repeat(GuardianContact.MAX_NAME_LENGTH), "", "").name().length());
        assertThrows(IllegalArgumentException.class, () ->
                new GuardianContact("A".repeat(GuardianContact.MAX_NAME_LENGTH + 1), "", ""));
        assertThrows(IllegalArgumentException.class, () ->
                new GuardianContact("", "1".repeat(Phone.MAX_LENGTH + 1), ""));
        assertThrows(IllegalArgumentException.class, () ->
                new GuardianContact("", "", "a".repeat(Email.MAX_LENGTH) + "@example.com"));
    }

    @Test
    public void independentOptionalFields_displayWithoutEmptySeparators() {
        GuardianContact nameOnly = new GuardianContact("Mary", "", "");
        GuardianContact emailOnly = new GuardianContact("", "", "mary@example.com");
        assertFalse(nameOnly.isEmpty());
        assertFalse(emailOnly.isEmpty());
        assertEquals("Mary", nameOnly.toString());
        assertEquals("mary@example.com", emailOnly.toString());
        assertEquals("", GuardianContact.EMPTY.toString());
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
