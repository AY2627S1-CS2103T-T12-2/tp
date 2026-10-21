package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

/** Exercises adding contact attributes through the real parser and model. */
public class AttributesIntegrationTest {

    private static final String LINK = "https://meet.example.com/math?code=123&room=2";
    private static final String ALICE = "add n/Alice p/91234567 e/alice@example.com a/One";
    private static final String BOB = "add n/Bob p/81234567 e/bob@example.com a/Two";
    private static final String MATH = " sub/Math cost/30 c/Monday1800-1930";
    private final AddressBookParser parser = new AddressBookParser();
    private ModelManager model;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
    }

    @Test
    public void add_fullAttributes_storesGuardianAndOnlineClassTogether() throws Exception {
        execute(ALICE + " gn/Mary Tan gp/98765432 ge/mary@example.com" + MATH
                + " mode/OnLiNe link/" + LINK + " location/Online room notes/Bring calculator");
        assertEquals(new GuardianContact("Mary Tan", "98765432", "mary@example.com"), student(0).getGuardianContact());
        assertEquals(new ClassDetails(ClassMode.ONLINE, LINK, "Online room", "Bring calculator"),
                lesson(0).getDetails());
        assertEquals(PaymentStatus.UNPAID, lesson(0).getPaymentStatus());
    }

    @Test
    public void add_optionalGuardianFields_canStoreOnlyPhoneOrNoGuardian() throws Exception {
        execute(ALICE + " gp/99999999");
        execute(BOB);
        assertEquals(new GuardianContact("", "99999999", ""), student(0).getGuardianContact());
        assertEquals(GuardianContact.EMPTY, student(1).getGuardianContact());
        assertTrue(student(0).getLessons().isEmpty());
    }

    @Test
    public void add_offlineOrUnspecifiedMode_defaultsWithoutLink() throws Exception {
        execute(ALICE + MATH + " mode/offline location/Room 2 notes/Bring notes");
        execute(BOB + " sub/Physics cost/40 c/Tuesday1800-1930");
        assertEquals(ClassMode.OFFLINE, lesson(0).getDetails().mode());
        assertEquals("", lesson(0).getDetails().meetingLink());
        assertEquals("Room 2", lesson(0).getDetails().location());
        assertEquals(ClassDetails.EMPTY, lesson(1).getDetails());
    }

    @Test
    public void add_onlineMode_allowsMissingMeetingLink() throws Exception {
        execute(ALICE + MATH + " mode/online");
        assertEquals(ClassMode.ONLINE, lesson(0).getDetails().mode());
        assertEquals("", lesson(0).getDetails().meetingLink());
    }

    @Test
    public void add_sameClass_inheritsDetailsWithoutChangingExistingContact() throws Exception {
        execute(ALICE + MATH + " mode/online link/" + LINK + " gn/Mary gp/99999999");
        execute("class payment 1 sub/Math c/Monday1800-1930 status/paid");
        Person alice = student(0);
        execute(BOB + MATH);
        assertEquals(alice, student(0));
        assertEquals(lesson(0).getDetails(), lesson(1).getDetails());
        assertEquals(PaymentStatus.PAID, lesson(0).getPaymentStatus());
        assertEquals(PaymentStatus.UNPAID, lesson(1).getPaymentStatus());
        assertEquals(GuardianContact.EMPTY, student(1).getGuardianContact());
        LocalDate mondayDate = LocalDate.of(2026, 10, 19);
        var monday = model.getTimetable(mondayDate).stream()
                .filter(day -> day.getDate().equals(mondayDate))
                .findFirst()
                .orElseThrow();
        assertEquals(1, monday.getEntries().size());
        assertEquals(2, monday.getEntries().getFirst().getStudents().size());
        assertEquals(LINK, monday.getEntries().getFirst().getLesson().getDetails().meetingLink());
    }

    @Test
    public void add_invalidAttributes_rejectedBeforeDataChanges() {
        List<String> invalid = List.of(
                ALICE + " gp/abc", ALICE + " gp/12", ALICE + " ge/invalid", ALICE + " gn/@Mary",
                ALICE + " gp/123 gp/456", ALICE + " gn/Mary gn/Ben", ALICE + " ge/a@b.com ge/c@d.com",
                ALICE + " mode/online", ALICE + " link/" + LINK,
                ALICE + " sub/Math cost/30 mode/online", ALICE + MATH + " mode/hybrid",
                ALICE + MATH + " mode/", ALICE + MATH + " mode/online mode/offline",
                ALICE + MATH + " mode/offline link/" + LINK,
                ALICE + MATH + " mode/online link/javascript:alert(1)",
                ALICE + MATH + " mode/online link/not-a-url", ALICE + MATH + " mode/online link/https://",
                ALICE + MATH + " mode/online link/https://example.com link/https://other.example.com",
                ALICE + MATH + " notes/" + "x".repeat(501));
        for (String command : invalid) {
            assertThrows(ParseException.class, () -> parser.parseCommand(command), command);
            assertTrue(model.getAddressBook().getPersonList().isEmpty());
        }
    }

    @Test
    public void add_invalidModeContextOrConflictingClass_doesNotAddContact() throws Exception {
        assertThrows(CommandException.class, () -> execute(ALICE + MATH + " link/" + LINK));
        assertTrue(model.getAddressBook().getPersonList().isEmpty());
        execute(ALICE + MATH + " mode/online link/" + LINK);
        AddressBook before = new AddressBook(model.getAddressBook());
        assertThrows(CommandException.class, () -> execute(BOB + MATH + " mode/offline"));
        assertThrows(CommandException.class, () -> execute(
                BOB + MATH + " mode/online link/https://example.com/other"));
        assertEquals(before, model.getAddressBook());
    }

    @Test
    public void existingCommands_keepOriginalSyntaxAndPreserveStoredAttributes() throws Exception {
        execute(ALICE + " gn/Mary gp/99999999" + MATH + " mode/online link/" + LINK);
        execute(BOB);
        GuardianContact guardian = student(0).getGuardianContact();
        ClassDetails details = lesson(0).getDetails();
        execute("edit 1 n/Alice Tan p/91111111");
        execute("tag 1 exam-prep");
        execute("class payment 1 sub/Math c/Monday1800-1930 status/paid");
        assertEquals(guardian, student(0).getGuardianContact());
        assertEquals(details, lesson(0).getDetails());
        execute("class enrol 2 sub/Math cost/30 c/Monday1800-1930");
        assertEquals(details, lesson(1).getDetails());
        assertEquals(PaymentStatus.UNPAID, lesson(1).getPaymentStatus());
        execute("class unenrol 1 sub/Math c/Monday1800-1930");
        assertEquals(guardian, student(0).getGuardianContact());
        assertTrue(student(0).getLessons().isEmpty());
    }

    @Test
    public void newFields_areNotExposedThroughEditOrEnrol() {
        for (String command : List.of("edit 1 gn/Mary", "edit 1 gp/99999999", "edit 1 ge/mary@example.com",
                "edit 1 sub/Math c/Monday1800-1930 mode/online",
                "class enrol 1 sub/Math cost/30 c/Monday1800-1930 mode/online link/" + LINK,
                "class details sub/Math c/Monday1800-1930 link/" + LINK)) {
            assertThrows(ParseException.class, () -> parser.parseCommand(command), command);
        }
    }

    private void execute(String command) throws Exception {
        parser.parseCommand(command).execute(model);
    }

    private Person student(int index) {
        return model.getAddressBook().getPersonList().get(index);
    }

    private Lesson lesson(int index) {
        return student(index).getLessons().iterator().next();
    }
}
