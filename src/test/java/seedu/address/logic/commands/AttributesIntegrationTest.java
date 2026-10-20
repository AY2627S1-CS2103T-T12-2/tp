package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassMode;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.Person;

/** Exercises the attributes through the application's real command parser and model. */
public class AttributesIntegrationTest {

    private static final String MATH = " sub/Math c/Monday1800-1930";
    private static final String LINK = "https://meet.example.com/math?code=123&room=2";
    private final AddressBookParser parser = new AddressBookParser();
    private ModelManager model;

    @BeforeEach
    public void setUp() throws Exception {
        model = new ModelManager();
        execute("add n/Alice p/91234567 e/alice@example.com a/One gn/Mary Tan gp/98765432 ge/mary@example.com");
        execute("add n/Bob p/81234567 e/bob@example.com a/Two");
        execute("add n/Cara p/71234567 e/cara@example.com a/Three");
        execute("class enrol 1 cost/30" + MATH);
        execute("class enrol 2 cost/30" + MATH);
        execute("class enrol 1 cost/40 sub/Physics c/Tuesday1800-1930");
    }

    @Test
    public void guardian_addAndEdit_allowPartialDetailsAndClearing() throws Exception {
        assertEquals(new GuardianContact("Mary Tan", "98765432", "mary@example.com"), student(0).getGuardianContact());
        assertEquals(GuardianContact.EMPTY, student(1).getGuardianContact());
        execute("edit 1 gp/88888888");
        assertEquals(new GuardianContact("Mary Tan", "88888888", "mary@example.com"), student(0).getGuardianContact());
        execute("edit 1 gn/ ge/");
        assertEquals(new GuardianContact("", "88888888", ""), student(0).getGuardianContact());
        execute("edit 1 gp/");
        assertEquals(GuardianContact.EMPTY, student(0).getGuardianContact());
        assertEquals(2, student(0).getLessons().size());
        execute("add n/Dan p/61234567 e/dan@example.com a/Four gp/99999999");
        assertEquals(new GuardianContact("", "99999999", ""), student(3).getGuardianContact());
    }

    @Test
    public void guardian_filteredEdit_usesDisplayedIndexAndPreservesOtherStudents() throws Exception {
        Person alice = student(0);
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Bob"));
        execute("edit 1 gn/Ben gp/99999999");
        assertEquals(new GuardianContact("Ben", "99999999", ""), student(1).getGuardianContact());
        assertEquals(alice, student(0));
    }

    @Test
    public void details_filteredUpdate_updatesWholeClassAndPreservesIndividualData() throws Exception {
        execute("class payment 1" + MATH + " status/paid");
        GuardianContact guardian = student(0).getGuardianContact();
        Lesson physics = student(0).getLessons().stream()
                .filter(lesson -> lesson.getSubject().value.equals("Physics")).findFirst().orElseThrow();
        model.updateFilteredPersonList(person -> person.getName().fullName.equals("Alice"));
        execute("edit 1 sub/math c/Monday1800-1930 mode/online link/" + LINK + " location/Room 2 notes/Bring notes");
        ClassDetails expected = new ClassDetails(LINK, "Room 2", "Bring notes");
        assertEquals(expected, math(0).getDetails());
        assertEquals(expected, math(1).getDetails());
        assertEquals(PaymentStatus.PAID, math(0).getPaymentStatus());
        assertEquals(PaymentStatus.UNPAID, math(1).getPaymentStatus());
        assertEquals(guardian, student(0).getGuardianContact());
        assertTrue(student(0).getLessons().contains(physics));
        assertEquals(new BigDecimal("40").stripTrailingZeros(), student(0).getOutstandingBalance());
        assertTrue(student(2).getLessons().isEmpty());
        var monday = model.getTimetable(LocalDate.of(2026, 10, 19)).getFirst();
        assertEquals(1, monday.getEntries().size());
        assertEquals(2, monday.getEntries().getFirst().getStudents().size());
        assertEquals(expected, monday.getEntries().getFirst().getLesson().getDetails());
    }

    @Test
    public void details_partialEditAndClear_preserveOmittedFields() throws Exception {
        execute("edit 1" + MATH + " mode/online link/" + LINK + " location/Room 2 notes/Bring notes");
        execute("edit 1" + MATH + " notes/Bring calculator");
        assertEquals(new ClassDetails(LINK, "Room 2", "Bring calculator"), math(0).getDetails());
        execute("edit 1" + MATH + " link/");
        assertEquals(new ClassDetails(ClassMode.ONLINE, "", "Room 2", "Bring calculator"), math(1).getDetails());
        execute("edit 1" + MATH + " location/ notes/");
        assertEquals(new ClassDetails(ClassMode.ONLINE, "", "", ""), math(0).getDetails());
        assertEquals(new ClassDetails(ClassMode.ONLINE, "", "", ""), math(1).getDetails());
    }

    @Test
    public void enrolAndAdd_existingClass_inheritSharedDetails() throws Exception {
        execute("edit 1" + MATH + " mode/online link/" + LINK);
        execute("class enrol 3 cost/30" + MATH);
        execute("add n/Dan p/61234567 e/dan@example.com a/Four sub/Math cost/30 c/Monday1800-1930 gp/99999999");
        for (int index = 0; index < 4; index++) {
            assertEquals(LINK, math(index).getDetails().meetingLink());
            assertEquals(PaymentStatus.UNPAID, math(index).getPaymentStatus());
        }
        assertEquals("99999999", student(3).getGuardianContact().phone());
    }

    @Test
    public void otherCommands_preserveAttributes() throws Exception {
        execute("edit 1" + MATH + " mode/online link/" + LINK);
        GuardianContact guardian = student(0).getGuardianContact();
        execute("class payment 1" + MATH + " status/paid");
        execute("edit 1 n/Alice Tan p/91111111");
        execute("tag 1 exam-prep");
        assertEquals(guardian, student(0).getGuardianContact());
        assertEquals(LINK, math(0).getDetails().meetingLink());
        assertEquals(PaymentStatus.PAID, math(0).getPaymentStatus());
        execute("class unenrol 1 sub/Physics c/Tuesday1800-1930");
        assertEquals(guardian, student(0).getGuardianContact());
        assertEquals(LINK, math(0).getDetails().meetingLink());
        execute("class delete" + MATH);
        assertEquals(guardian, student(0).getGuardianContact());
        assertTrue(student(0).getLessons().isEmpty());
    }

    @Test
    public void invalidInputs_rejectedWithoutChangingData() {
        AddressBook before = new AddressBook(model.getAddressBook());
        List<String> invalid = List.of(
                "edit 1" + MATH,
                "edit 1 1" + MATH + " link/" + LINK,
                "edit 1 sub/Math link/" + LINK,
                "edit 1 c/Monday1800-1930 link/" + LINK,
                "edit 1 sub/ c/Monday1800-1930 link/" + LINK,
                "edit 1 sub/Math c/Monday2400-2500 link/" + LINK,
                "edit 1" + MATH + " link/not-a-url",
                "edit 1" + MATH + " link/javascript:alert(1)",
                "edit 1" + MATH + " link/https://",
                "edit 1" + MATH + " link/https://example.com link/https://other.example.com",
                "edit 1" + MATH + " notes/" + "x".repeat(501),
                "edit 1 mode/online", "edit 1" + MATH + " mode/hybrid",
                "edit 1" + MATH + " mode/", "edit 1" + MATH + " mode/online mode/offline",
                "edit 1" + MATH + " mode/offline link/" + LINK,
                "add n/Dan p/61234567 e/dan@example.com a/Four mode/online",
                "class details" + MATH + " link/" + LINK,
                "edit 1 gp/abc", "edit 1 ge/invalid", "edit 1 gn/@Mary", "edit 1 gp/123 gp/456",
                "add n/Dan p/61234567 e/dan@example.com a/Four gp/abc",
                "add n/Dan p/61234567 e/dan@example.com a/Four gn/Mary gn/Ben");
        for (String command : invalid) {
            assertThrows(ParseException.class, () -> parser.parseCommand(command), command);
            assertEquals(before, model.getAddressBook());
        }
    }

    @Test
    public void missingClassOrInvalidIndex_rejectedWithoutChangingData() {
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> execute(
                "edit 1 sub/English c/Friday1800-1930 notes/Test"));
        assertThrows(CommandException.class, () -> execute("edit 99 gn/Mary"));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void commandEquality_includesGuardianAndDetailsChanges() throws Exception {
        assertEquals(parser.parseCommand("edit 1 gn/Mary"), parser.parseCommand("edit 1 gn/Mary"));
        assertNotEquals(parser.parseCommand("edit 1 gn/Mary"), parser.parseCommand("edit 1 gn/Ben"));
        assertNotEquals(parser.parseCommand("edit 1 gp/123"), parser.parseCommand("edit 1 gp/456"));
        assertNotEquals(parser.parseCommand("edit 1 ge/a@b.com"), parser.parseCommand("edit 1 ge/c@b.com"));
        assertEquals(parser.parseCommand("edit 1" + MATH + " mode/online"),
                parser.parseCommand("edit 1" + MATH + " mode/online"));
        assertNotEquals(parser.parseCommand("edit 1" + MATH + " mode/online"),
                parser.parseCommand("edit 1" + MATH + " mode/offline"));
    }

    @Test
    public void mode_onlineAndOffline_changesInlineAndClearsLink() throws Exception {
        execute("edit 1" + MATH + " mode/OnLiNe link/" + LINK);
        assertEquals(ClassMode.ONLINE, math(0).getDetails().mode());
        execute("edit 1" + MATH + " link/https://example.com/replacement");
        assertEquals("https://example.com/replacement", math(1).getDetails().meetingLink());
        execute("edit 1" + MATH + " mode/offline");
        assertEquals(ClassDetails.EMPTY, math(0).getDetails());
        assertEquals(ClassDetails.EMPTY, math(1).getDetails());
        execute("edit 1" + MATH + " mode/online");
        assertEquals(ClassMode.ONLINE, math(0).getDetails().mode());
        assertEquals("", math(0).getDetails().meetingLink());
    }

    @Test
    public void mode_addAndEnrol_acceptInlineFieldsOnNewClasses() throws Exception {
        execute("add n/Dan p/61234567 e/dan@example.com a/Four sub/English cost/20 c/Thursday1800-1930"
                + " mode/online link/" + LINK + " gn/Ben gp/99999999");
        Lesson english = student(3).getLessons().iterator().next();
        assertEquals(ClassMode.ONLINE, english.getDetails().mode());
        assertEquals(LINK, english.getDetails().meetingLink());
        assertEquals("Ben", student(3).getGuardianContact().name());
        execute("class enrol 3 sub/Art cost/10 c/Friday1800-1930 mode/offline location/Room 1");
        Lesson art = student(2).getLessons().iterator().next();
        assertEquals(ClassMode.OFFLINE, art.getDetails().mode());
        assertEquals("", art.getDetails().meetingLink());
        assertEquals("Room 1", art.getDetails().location());
    }

    @Test
    public void mode_invalidContextAndConflictingEnrolment_doNotMutateData() throws Exception {
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> execute("edit 1" + MATH + " link/" + LINK));
        assertThrows(CommandException.class, () -> execute("class enrol 3 cost/30" + MATH + " mode/online"));
        assertThrows(CommandException.class, () -> execute(
                "add n/Dan p/61234567 e/dan@example.com a/Four sub/Math cost/30 c/Monday1800-1930 mode/online"));
        assertThrows(CommandException.class, () -> execute("edit 3 n/Changed" + MATH + " mode/online"));
        assertEquals(before, model.getAddressBook());
    }

    private void execute(String command) throws Exception {
        parser.parseCommand(command).execute(model);
    }

    private Person student(int index) {
        return model.getAddressBook().getPersonList().get(index);
    }

    private Lesson math(int index) {
        return student(index).getLessons().stream().filter(lesson -> lesson.getSubject().value.equals("Math"))
                .findFirst().orElseThrow();
    }
}
