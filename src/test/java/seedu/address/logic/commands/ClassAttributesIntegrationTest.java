package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.ClassMode;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.person.GuardianContact;

/** Verifies attributes against the independently stored class model from PR 88. */
public class ClassAttributesIntegrationTest {

    private static final String MATH = " sub/Math cost/30 c/Saturday1800-1930";
    private static final String ALICE = "add n/Alice p/91234567 e/alice@example.com a/One gn/Mary gp/99999999";
    private static final String LINK = "https://meet.example.com/math";
    private static final ClassDetails DETAILS = new ClassDetails(ClassMode.ONLINE, LINK, "Room 2", "Bring notes");
    private final AddressBookParser parser = new AddressBookParser();
    private ModelManager model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
    }

    @Test
    public void addContact_createsCanonicalClassAndLinkedEnrolmentWithAttributes() throws Exception {
        addAlice();
        assertEquals(DETAILS, model.getTuitionClassList().getFirst().getDetails());
        assertEquals(DETAILS, firstLesson().getDetails());
        assertTrue(firstLesson() instanceof Enrollment);
        assertEquals(new ClassId("C001"), firstLesson().getClassId());
        assertEquals(new GuardianContact("Mary", "99999999", ""),
                model.getAddressBook().getPersonList().getFirst().getGuardianContact());
        assertEquals(firstLesson().getClassId(), firstLesson().withPaymentStatus(PaymentStatus.PAID).getClassId());
        assertEquals(DETAILS, firstLesson().withPaymentStatus(PaymentStatus.PAID).getDetails());
    }

    @Test
    public void firstContact_canFillDetailsOfAnEmptyUndescribedClass() throws Exception {
        execute("class add" + MATH);
        addAlice();
        assertEquals(1, model.getTuitionClassList().size());
        assertEquals(new ClassId("C001"), firstLesson().getClassId());
        assertEquals(DETAILS, model.getTuitionClassList().getFirst().getDetails());
        assertEquals(DETAILS, firstLesson().getDetails());
        assertEquals(2, model.getAddressBook().getNextClassSequence());
    }

    @Test
    public void classEditAndIdEnrolment_preserveAttributesAndSeparatePayments() throws Exception {
        addAlice();
        execute("add n/Bob p/81234567 e/bob@example.com a/Two gp/88888888");
        execute("class payment 1 id/C001 status/paid");
        execute("class enrol 2 id/C001");
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Alice"));
        execute("class edit id/C001 sub/Advanced Math cost/35 c/Saturday1900-2030");
        for (var person : model.getAddressBook().getPersonList()) {
            Lesson lesson = person.getLessons().iterator().next();
            assertEquals(DETAILS, lesson.getDetails());
            assertEquals(new ClassId("C001"), lesson.getClassId());
            assertEquals("Advanced Math", lesson.getSubject().value);
            assertEquals("35", lesson.getCost().toString());
        }
        assertEquals(PaymentStatus.PAID, firstLesson().getPaymentStatus());
        var bob = model.getAddressBook().getPersonList().get(1);
        assertEquals(PaymentStatus.UNPAID, bob.getLessons().iterator().next().getPaymentStatus());
        assertEquals("88888888", bob.getGuardianContact().phone());
        assertEquals(DETAILS, model.getTuitionClassList().getFirst().getDetails());
    }

    @Test
    public void lastUnenrolment_retainsSharedDetailsForLaterContactAndTimetable() throws Exception {
        addAlice();
        execute("class unenrol 1 id/C001");
        assertEquals(DETAILS, model.getTuitionClassList().getFirst().getDetails());
        LocalDate saturdayDate = LocalDate.of(2026, 10, 24);
        var saturday = model.getTimetable(saturdayDate).stream()
                .filter(day -> day.getDate().equals(saturdayDate))
                .findFirst()
                .orElseThrow();
        assertEquals(1, saturday.getEntries().size());
        assertTrue(saturday.getEntries().getFirst().getStudents().isEmpty());
        assertEquals(DETAILS, saturday.getEntries().getFirst().getLesson().getDetails());
        execute("add n/Bob p/81234567 e/bob@example.com a/Two" + MATH);
        assertEquals(DETAILS, model.getAddressBook().getPersonList().get(1)
                .getLessons().iterator().next().getDetails());
    }

    @Test
    public void oneOffClassAndBatchCommands_preserveSharedDetailsAndPinnedGuardian() throws Exception {
        String schedule = "2026-10-24T1800-1930";
        execute(ALICE + " sub/Math cost/30 c/" + schedule + " mode/online link/" + LINK);
        execute("add n/Bob p/81234567 e/bob@example.com a/Two gp/88888888");
        execute("pin 1");
        execute("class enrol 1 2 sub/Math c/" + schedule);
        execute("tag 1 2 t/exam-prep");
        execute("edit 1 p/91111111");
        execute("class payment 1 id/C001 status/paid");
        execute("class edit id/C001 c/2026-10-24T1900-2030");
        var alice = model.getAddressBook().getPersonList().getFirst();
        assertTrue(alice.isPinned());
        assertEquals("99999999", alice.getGuardianContact().phone());
        assertEquals(PaymentStatus.PAID, firstLesson().getPaymentStatus());
        assertEquals(LINK, firstLesson().getDetails().meetingLink());
        assertEquals("2026-10-24T1900-2030", firstLesson().getSchedule().toStorageString());
        LocalDate saturdayDate = LocalDate.of(2026, 10, 24);
        var saturday = model.getTimetable(saturdayDate).stream()
                .filter(day -> day.getDate().equals(saturdayDate))
                .findFirst()
                .orElseThrow();
        assertEquals(1, saturday.getEntries().size());
        assertTrue(model.getTimetable(LocalDate.of(2026, 10, 25)).stream()
                .allMatch(day -> day.getEntries().isEmpty()));
        execute("class delete id/C001");
        assertTrue(model.getAddressBook().getPersonList().getFirst().isPinned());
        assertEquals("99999999", model.getAddressBook().getPersonList().getFirst().getGuardianContact().phone());
    }

    @Test
    public void sameSubjectAtDifferentTimes_createsSeparateClassDetails() throws Exception {
        addAlice();
        execute("add n/Bob p/81234567 e/bob@example.com a/Two"
                + " sub/Math cost/30 c/Saturday1600-1730 mode/offline location/Room 3");
        assertEquals(2, model.getTuitionClassList().size());
        assertEquals(DETAILS, firstLesson().getDetails());
        assertEquals("Room 3", model.getAddressBook().getPersonList().get(1)
                .getLessons().iterator().next().getDetails().location());
        assertNotEquals(parser.parseCommand(ALICE + MATH), parser.parseCommand(ALICE + MATH + " mode/online"));
    }

    @Test
    public void failedContactAdd_leavesEmptyClassAndSequenceUnchanged() throws Exception {
        execute("class add" + MATH);
        execute(ALICE);
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, this::addAlice);
        assertEquals(before, model.getAddressBook());
        assertEquals(ClassDetails.EMPTY, model.getTuitionClassList().getFirst().getDetails());
    }

    @Test
    public void conflictingDetailsForOccupiedClass_leaveAllDataUnchanged() throws Exception {
        execute(ALICE + MATH);
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> execute(
                "add n/Bob p/81234567 e/bob@example.com a/Two" + MATH + " mode/online link/" + LINK));
        assertEquals(before, model.getAddressBook());
    }

    private void addAlice() throws Exception {
        execute(ALICE + MATH + " mode/online link/" + LINK + " location/Room 2 notes/Bring notes");
    }

    private void execute(String command) throws Exception {
        parser.parseCommand(command).execute(model);
    }

    private Lesson firstLesson() {
        return model.getAddressBook().getPersonList().getFirst().getLessons().iterator().next();
    }
}
