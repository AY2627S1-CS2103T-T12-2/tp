package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_EMAIL_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_NAME_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_PHONE_BOB;
import static seedu.address.logic.commands.CommandTestUtil.VALID_TAG_HUSBAND;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BOB;

import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;
import seedu.address.testutil.PersonBuilder;

public class PersonTest {

    @Test
    public void hasUnmigratedLegacyLesson_usesTheSameBoundaryAsMigration() {
        var cost = Optional.of(new seedu.address.model.person.Cost("30"));
        Person lastMinute = legacyStudent(cost, Optional.of(new seedu.address.model.person.Lesson("Monday2359")));
        Person lastMigratableMinute = legacyStudent(cost,
                Optional.of(new seedu.address.model.person.Lesson("Monday2358")));
        assertTrue(lastMinute.getLessons().isEmpty());
        assertTrue(lastMinute.hasUnmigratedLegacyLesson());
        assertEquals("Monday2358-2359", lastMigratableMinute.getLessons().iterator().next()
                .getSchedule().toStorageString());
        assertFalse(lastMigratableMinute.hasUnmigratedLegacyLesson());
        assertFalse(lastMigratableMinute.withLessons(Set.of()).hasUnmigratedLegacyLesson());
        assertFalse(lastMinute.withLessons(lastMigratableMinute.getLessons()).hasUnmigratedLegacyLesson());
    }

    @Test
    public void hasUnmigratedLegacyLesson_partialRecordsRetainSubjectsWithoutInventingClasses() {
        Person missingCost = legacyStudent(Optional.empty(),
                Optional.of(new seedu.address.model.person.Lesson("Monday1800")));
        Person missingStart = legacyStudent(Optional.of(new seedu.address.model.person.Cost("30")), Optional.empty());
        assertTrue(missingCost.hasUnmigratedLegacyLesson());
        assertTrue(missingStart.hasUnmigratedLegacyLesson());
        assertFalse(new PersonBuilder().build().hasUnmigratedLegacyLesson());
    }

    private Person legacyStudent(Optional<seedu.address.model.person.Cost> cost,
            Optional<seedu.address.model.person.Lesson> lesson) {
        Person base = new PersonBuilder().build();
        return new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(), base.getTags(),
                Set.of(new seedu.address.model.person.Subject("Math")), cost, lesson);
    }

    @Test
    public void constructor_moreThanTenTags_acceptsLegacyData() {
        Person person = new PersonBuilder()
                .withTags("one", "two", "three", "four", "five", "six", "seven", "eight", "nine", "ten",
                        "eleven")
                .build();

        assertEquals(11, person.getTags().size());
    }

    @Test
    public void asObservableList_modifyList_throwsUnsupportedOperationException() {
        Person person = new PersonBuilder().build();
        assertThrows(UnsupportedOperationException.class, () -> person.getTags().remove(0));
    }

    @Test
    public void getOutstandingBalance_noUnpaidLessons_returnsZero() {
        Lesson paidLesson = lesson("Math", "30", "Monday1800-1930")
                .withPaymentStatus(PaymentStatus.PAID);
        Person person = new PersonBuilder().withLessons(paidLesson).build();

        assertEquals("0", person.getOutstandingBalance().toPlainString());
    }

    @Test
    public void getOutstandingBalance_paidAndUnpaidLessons_sumsOnlyUnpaidCosts() {
        Lesson math = lesson("Math", "30", "Monday1800-1930");
        Lesson computing = lesson("Computing", "60.50", "Tuesday1500-1700");
        Lesson paidPhysics = lesson("Physics", "35", "Wednesday1700-1830")
                .withPaymentStatus(PaymentStatus.PAID);
        Person person = new PersonBuilder().withLessons(math, computing, paidPhysics).build();

        assertEquals("90.5", person.getOutstandingBalance().toPlainString());
    }

    @Test
    public void withPinned_updatesOnlyPinnedState() {
        Person pinnedAlice = ALICE.withPinned(true);

        assertTrue(pinnedAlice.isPinned());
        assertTrue(ALICE.isSamePerson(pinnedAlice));
        assertEquals(ALICE.getName(), pinnedAlice.getName());
        assertEquals(ALICE.getPhone(), pinnedAlice.getPhone());
        assertEquals(ALICE.getEmail(), pinnedAlice.getEmail());
        assertEquals(ALICE.getAddress(), pinnedAlice.getAddress());
        assertEquals(ALICE.getTags(), pinnedAlice.getTags());
        assertEquals(ALICE.getLessons(), pinnedAlice.getLessons());
    }

    @Test
    public void copyOperations_pinnedPerson_preservePinnedState() {
        Person pinnedPerson = new PersonBuilder(ALICE).withPinned(true).build();

        assertTrue(pinnedPerson.withTags(pinnedPerson.getTags()).isPinned());
        assertTrue(pinnedPerson.withLessons(pinnedPerson.getLessons()).isPinned());
        assertTrue(new PersonBuilder(pinnedPerson).build().isPinned());
    }

    @Test
    public void isSamePerson() {
        // same object -> returns true
        assertTrue(ALICE.isSamePerson(ALICE));

        // null -> returns false
        assertFalse(ALICE.isSamePerson(null));

        // same name, all other attributes different -> returns true
        Person editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).withEmail(VALID_EMAIL_BOB)
                .withAddress(VALID_ADDRESS_BOB).withTags(VALID_TAG_HUSBAND).build();
        assertTrue(ALICE.isSamePerson(editedAlice));

        // different name, all other attributes same -> returns false
        editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.isSamePerson(editedAlice));

        // name differs in case, all other attributes same -> returns false
        Person editedBob = new PersonBuilder(BOB).withName(VALID_NAME_BOB.toLowerCase()).build();
        assertFalse(BOB.isSamePerson(editedBob));

        // name has trailing spaces, all other attributes same -> returns false
        String nameWithTrailingSpaces = VALID_NAME_BOB + " ";
        editedBob = new PersonBuilder(BOB).withName(nameWithTrailingSpaces).build();
        assertFalse(BOB.isSamePerson(editedBob));
    }

    @Test
    public void equals() {
        // same values -> returns true
        Person aliceCopy = new PersonBuilder(ALICE).build();
        assertTrue(ALICE.equals(aliceCopy));

        // same object -> returns true
        assertTrue(ALICE.equals(ALICE));

        // null -> returns false
        assertFalse(ALICE.equals(null));

        // different type -> returns false
        assertFalse(ALICE.equals(5));

        // different person -> returns false
        assertFalse(ALICE.equals(BOB));

        // different name -> returns false
        Person editedAlice = new PersonBuilder(ALICE).withName(VALID_NAME_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different phone -> returns false
        editedAlice = new PersonBuilder(ALICE).withPhone(VALID_PHONE_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different email -> returns false
        editedAlice = new PersonBuilder(ALICE).withEmail(VALID_EMAIL_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different address -> returns false
        editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        assertFalse(ALICE.equals(editedAlice));

        // different tags -> returns false
        editedAlice = new PersonBuilder(ALICE).withTags(VALID_TAG_HUSBAND).build();
        assertFalse(ALICE.equals(editedAlice));

        // different pin state -> returns false
        editedAlice = new PersonBuilder(ALICE).withPinned(true).build();
        assertFalse(ALICE.equals(editedAlice));
    }

    @Test
    public void toStringMethod() {
        String expected = Person.class.getCanonicalName()
                + "{name=" + ALICE.getName()
                + ", phone=" + ALICE.getPhone()
                + ", email=" + ALICE.getEmail()
                + ", address=" + ALICE.getAddress()
                + ", tags=" + ALICE.getTags()
                + ", subjects=" + ALICE.getSubjects()
                + ", cost=" + ALICE.getCost()
                + ", lesson=" + ALICE.getLesson()
                + ", pinned=" + ALICE.isPinned() + "}";
        assertEquals(expected, ALICE.toString());
    }

    private static Lesson lesson(String subject, String cost, String timing) {
        return new Lesson(new Subject(subject), new Cost(cost), new LessonTiming(timing));
    }
}
