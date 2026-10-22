package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.ClassMode;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.testutil.PersonBuilder;

public class ClassAttributesModelTest {

    private static final ClassId CLASS_ID = new ClassId("C001");
    private static final Subject SUBJECT = new Subject("Math");
    private static final LessonTiming TIMING = new LessonTiming("Monday1800-1930");
    private static final ClassDetails ONLINE = new ClassDetails(ClassMode.ONLINE, "https://example.com/math", "", "");

    @Test
    public void addLegacyEnrolment_promotesDetailsOnEmptyClassAndRetainsGuardianAndPin() {
        ModelManager model = modelWithClass(ClassDetails.EMPTY);
        Lesson lesson = new Lesson(SUBJECT, new Cost("30"), TIMING, PaymentStatus.PAID, ONLINE);
        var person = new PersonBuilder().withPinned(true).withGuardianContact("Mary", "99999999", "")
                .withLessons(lesson).build();
        model.addPerson(person);
        var linked = model.getAddressBook().getPersonList().getFirst();
        assertEquals(ONLINE, model.getTuitionClass(CLASS_ID).getDetails());
        assertEquals(CLASS_ID, linked.getLessons().iterator().next().getClassId());
        assertEquals(person.getGuardianContact(), linked.getGuardianContact());
        assertEquals(person.isPinned(), linked.isPinned());
    }

    @Test
    public void differentAgreedRateAcceptedButExplicitMetadataRejected() {
        ModelManager model = modelWithClass(ONLINE);
        Lesson wrongCost = new Lesson(SUBJECT, new Cost("40"), TIMING);
        model.addPerson(new PersonBuilder().withLessons(wrongCost).build());
        assertEquals(new Cost("40"), model.getAddressBook().getPersonList().getFirst()
                .getEnrollments().iterator().next().getAgreedRate());

        AddressBook before = new AddressBook(model.getAddressBook());
        Lesson wrongDetails = new Lesson(SUBJECT, new Cost("30"), TIMING,
                PaymentStatus.UNPAID, new ClassDetails("", "Room 2", ""));
        assertThrows(IllegalArgumentException.class, () -> model.addPerson(
                new PersonBuilder().withName("Bob").withLessons(wrongDetails).build()));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void occupiedUndescribedClass_rejectsMetadataReplacement() {
        ModelManager model = modelWithClass(ClassDetails.EMPTY);
        model.addPerson(new PersonBuilder().withName("Alice")
                .withLessons(new Lesson(SUBJECT, new Cost("30"), TIMING)).build());
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(IllegalArgumentException.class, () -> model.addPerson(new PersonBuilder().withName("Bob")
                .withLessons(new Lesson(SUBJECT, new Cost("30"), TIMING, PaymentStatus.UNPAID, ONLINE)).build()));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void otherClassStudents_doNotPreventEmptyClassEnrichment() {
        ModelManager model = modelWithClass(ClassDetails.EMPTY);
        model.addPerson(new PersonBuilder().withName("Alice").withLessons(new Lesson(
                new Subject("Physics"), new Cost("20"), new LessonTiming("Tuesday1800-1930"))).build());
        var alice = model.getAddressBook().getPersonList().getFirst();
        model.addPerson(new PersonBuilder().withName("Bob")
                .withLessons(new Lesson(SUBJECT, new Cost("30"), TIMING, PaymentStatus.UNPAID, ONLINE)).build());
        assertEquals(alice, model.getAddressBook().getPersonList().getFirst());
        assertEquals(ONLINE, model.getTuitionClass(CLASS_ID).getDetails());
    }

    private ModelManager modelWithClass(ClassDetails details) {
        AddressBook book = new AddressBook();
        book.addTuitionClass(new TuitionClass(CLASS_ID, SUBJECT, new Cost("30"), TIMING, details));
        return new ModelManager(book, new UserPrefs());
    }
}
