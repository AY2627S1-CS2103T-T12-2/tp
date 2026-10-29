package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX;
import static seedu.address.logic.Messages.MESSAGE_UNKNOWN_COMMAND;
import static seedu.address.logic.commands.CommandTestUtil.ADDRESS_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.EMAIL_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.NAME_DESC_AMY;
import static seedu.address.logic.commands.CommandTestUtil.PHONE_DESC_AMY;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.AMY;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Comparator;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.GuiSettings;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.ClearCommand;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.HelpCommand;
import seedu.address.logic.commands.ListCommand;
import seedu.address.logic.commands.PrivacyCommand;
import seedu.address.logic.commands.TimetableCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.EditCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.ReadOnlyUserPrefs;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;
import seedu.address.model.timetable.TimetableDay;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

public class LogicManagerTest {
    private static final IOException DUMMY_IO_EXCEPTION = new IOException("dummy IO exception");
    private static final IOException DUMMY_AD_EXCEPTION = new AccessDeniedException("dummy access denied exception");

    @TempDir
    public Path temporaryFolder;

    private Model model = new ModelManager();
    private Logic logic;
    private StorageManager storage;

    @BeforeEach
    public void setUp() {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage = new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json"));
        storage = new StorageManager(addressBookStorage, userPrefsStorage);
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_invalidCommandFormat_throwsParseException() {
        String invalidCommand = "uicfhmowqewca";
        assertParseException(invalidCommand, MESSAGE_UNKNOWN_COMMAND);
    }

    @Test
    public void execute_commandExecutionError_throwsCommandException() {
        String deleteCommand = "delete 9";
        assertCommandException(deleteCommand, MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
    }

    @Test
    public void execute_validCommand_success() throws Exception {
        String listCommand = ListCommand.COMMAND_WORD;
        assertCommandSuccess(listCommand, ListCommand.MESSAGE_SUCCESS, model);
    }

    @Test
    public void execute_batchEnrolmentSubjectWithRepeatedSpaces_findsExistingClass() throws Exception {
        logic.execute("add n/Amy p/91234567 e/amy@example.com a/Somewhere");
        logic.execute("add n/Bob p/81234567 e/bob@example.com a/Elsewhere");
        logic.execute("class add sub/Computer Science cost/30 c/Friday1400-1500");

        CommandResult result = logic.execute(
                "class enrol 1 2 sub/Computer  Science c/Friday1400-1500");

        assertEquals("Enrolled Amy, Bob in class: Computer Science, Friday 14:00-15:00.",
                result.getFeedbackToUser());
        assertTrue(model.getAddressBook().getPersonList().stream()
                .allMatch(person -> person.getLessons().stream()
                        .anyMatch(lesson -> lesson.getSubject().equals(new Subject("Computer Science")))));
    }

    @Test
    public void execute_unsupportedGuardianEditAfterAddress_preservesStudentAndSavedData() throws Exception {
        logic.execute("add n/Alice p/91234567 e/alice@example.com a/One gp/99999999");
        Person originalStudent = model.getAddressBook().getPersonList().getFirst();
        String originalJson = Files.readString(storage.getAddressBookFilePath());

        assertParseException("edit 1 a/Two gp/88888888", EditCommandParser.MESSAGE_UNSUPPORTED_GUARDIAN_EDIT);

        assertEquals(originalStudent, model.getAddressBook().getPersonList().getFirst());
        assertEquals(originalJson, Files.readString(storage.getAddressBookFilePath()));
        assertEquals(originalStudent, storage.readAddressBook().orElseThrow().getPersonList().getFirst());
    }

    @Test
    public void execute_unsupportedClassEdits_preserveDataFilterAndSavedFile() throws Exception {
        logic.execute("add n/Alice p/91234567 e/alice@example.com a/One gp/99999999"
                + " sub/Math cost/30 c/Monday1800-1900 mode/online notes/Review");
        logic.execute("add n/Bob p/81234567 e/bob@example.com a/Elsewhere");
        logic.execute("class payment 1 id/C001 status/paid");
        logic.execute("pin 1");
        logic.execute("find sub/Math");
        AddressBook original = new AddressBook(model.getAddressBook());
        List<Person> originalResults = List.copyOf(model.getFilteredPersonList());
        String originalJson = Files.readString(storage.getAddressBookFilePath());
        String expected = "Class fields cannot be changed with edit. "
                + "Remove them and use a supported edit field.";

        for (String field : List.of("sub/Physics", "cost/40", "c/Tuesday1800-1900", "mode/offline",
                "link/https://example.com", "location/Room 2", "notes/Bring notes", "id/C001", "status/unpaid",
                "sub/Math cost/30 c/Monday1800-1900 mode/online", "\tsub/Physics")) {
            assertThrows(ParseException.class, expected, () -> logic.execute("edit 1 a/Two " + field));
            assertEquals(original, model.getAddressBook());
            assertEquals(originalResults, model.getFilteredPersonList());
            assertEquals(originalJson, Files.readString(storage.getAddressBookFilePath()));
            assertEquals(original, storage.readAddressBook().orElseThrow());
        }
    }

    @Test
    public void privacyMode_getAndSet_delegatesToModel() {
        assertFalse(logic.isPrivacyModeEnabled());

        logic.setPrivacyModeEnabled(true);

        assertTrue(logic.isPrivacyModeEnabled());
        assertTrue(model.isPrivacyModeEnabled());
    }

    @Test
    public void execute_privacyCommand_persistsAcrossRestart() throws Exception {
        assertEquals(PrivacyCommand.MESSAGE_PRIVACY_MODE_ENABLED,
                logic.execute("privacy on").getFeedbackToUser());

        UserPrefs reloadedPrefs = storage.readUserPrefs().orElseThrow();
        Logic restartedLogic = new LogicManager(new ModelManager(model.getAddressBook(), reloadedPrefs), storage);

        assertTrue(restartedLogic.isPrivacyModeEnabled());
    }

    @Test
    public void execute_userPrefsStorageThrowsIoException_nonPrivacyCommandSucceeds() throws Exception {
        JsonAddressBookStorage addressBookStorage =
                new JsonAddressBookStorage(temporaryFolder.resolve("addressBook.json"));
        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("userPrefs.json")) {
                    @Override
                    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
                        throw DUMMY_IO_EXCEPTION;
                    }
                };
        logic = new LogicManager(model, new StorageManager(addressBookStorage, userPrefsStorage));

        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        Person expectedPerson = new PersonBuilder(AMY).withTags().build();

        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS, Messages.format(expectedPerson)),
                logic.execute(addCommand).getFeedbackToUser());
        assertTrue(model.hasPerson(expectedPerson));
    }

    @Test
    public void execute_storageThrowsIoException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_IO_EXCEPTION, String.format(
                LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_storageThrowsAdException_throwsCommandException() {
        assertCommandFailureForExceptionFromStorage(DUMMY_AD_EXCEPTION, String.format(
                LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, DUMMY_AD_EXCEPTION.getMessage()));
    }

    @Test
    public void execute_paymentSaveFails_rollsBackInMemoryAndKeepsPersistedPaymentState() throws Exception {
        logic.execute("add n/Alex p/91234567 e/alex@example.com a/One "
                + "sub/Mathematics cost/45 c/Monday1800-1930 mode/online link/https://example.com/math");
        logic.execute("class payment 1 id/C001 status/paid");

        ReadOnlyAddressBook persistedBeforeFailure = storage.readAddressBook().orElseThrow();
        assertPaymentState(persistedBeforeFailure.getPersonList().get(0), PaymentStatus.PAID, "0");
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs(model.getUserPrefs()));

        String permissionFailure = "payment save denied";
        JsonAddressBookStorage failingAddressBookStorage =
                new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                        throw new AccessDeniedException(permissionFailure);
                    }
                };
        logic = new LogicManager(model, new StorageManager(failingAddressBookStorage,
                new JsonUserPrefsStorage(storage.getUserPrefsFilePath())));

        String expectedMessage = String.format(LogicManager.FILE_OPS_PERMISSION_ERROR_FORMAT, permissionFailure);
        assertThrows(CommandException.class, expectedMessage, () ->
                logic.execute("class payment 1 id/C001 status/unpaid"));

        assertEquals(expectedModel, model);
        assertPaymentState(model.getFilteredPersonList().get(0), PaymentStatus.PAID, "0");
        assertEquals(persistedBeforeFailure, storage.readAddressBook().orElseThrow());
        assertPaymentState(storage.readAddressBook().orElseThrow().getPersonList().get(0), PaymentStatus.PAID, "0");
    }

    @Test
    public void execute_privacyPrefsSaveFails_restoresInMemoryAndPersistedPrivacyMode() throws Exception {
        storage.saveUserPrefs(new UserPrefs());
        String saveFailure = "preference save denied";
        JsonUserPrefsStorage failingUserPrefsStorage =
                new JsonUserPrefsStorage(storage.getUserPrefsFilePath()) {
                    @Override
                    public void saveUserPrefs(ReadOnlyUserPrefs userPrefs) throws IOException {
                        throw new IOException(saveFailure);
                    }
                };
        storage = new StorageManager(new JsonAddressBookStorage(storage.getAddressBookFilePath()),
                failingUserPrefsStorage);
        logic = new LogicManager(model, storage);

        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, saveFailure);
        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("privacy on"));

        assertFalse(model.isPrivacyModeEnabled());
        assertFalse(logic.isPrivacyModeEnabled());
        assertFalse(storage.readUserPrefs().orElseThrow().isPrivacyModeEnabled());
    }

    @Test
    public void execute_saveFails_restoresFilteredPersonListPredicateAndComparator() {
        Person amy = new PersonBuilder().withName("Amy").withPhone("91234567")
                .withEmail("amy@example.com").build();
        Person bob = new PersonBuilder().withName("Bob").withPhone("92345678")
                .withEmail("bob@example.com").build();
        Person carol = new PersonBuilder().withName("Carol").withPhone("93456789")
                .withEmail("carol@example.com").build();
        model.addPerson(amy);
        model.addPerson(bob);
        model.addPerson(carol);
        model.updateFilteredPersonList(person -> !person.getName().fullName.equals("Carol"),
                Comparator.comparing((Person person) -> person.getName().fullName).reversed());

        JsonAddressBookStorage failingAddressBookStorage =
                new JsonAddressBookStorage(storage.getAddressBookFilePath()) {
                    @Override
                    public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                        throw DUMMY_IO_EXCEPTION;
                    }
                };
        logic = new LogicManager(model, new StorageManager(failingAddressBookStorage,
                new JsonUserPrefsStorage(storage.getUserPrefsFilePath())));

        String expectedMessage = String.format(LogicManager.FILE_OPS_ERROR_FORMAT, DUMMY_IO_EXCEPTION.getMessage());
        assertThrows(CommandException.class, expectedMessage, () -> logic.execute("find Carol"));

        assertEquals(List.of(bob, amy), model.getFilteredPersonList());
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> logic.getFilteredPersonList().remove(0));
    }

    @Test
    public void execute_readOnlyViews_onlyNonModifyingCommandsAllowed() throws Exception {
        CommandResult timetableResult = logic.execute(TimetableCommand.COMMAND_WORD);
        assertEquals(CommandResult.ViewChange.TIMETABLE, timetableResult.getViewChange());

        assertCommandException("clear", LogicManager.MESSAGE_TIMETABLE_READ_ONLY);
        assertCommandException("find Alice", LogicManager.MESSAGE_TIMETABLE_READ_ONLY);
        assertCommandException("timetable", LogicManager.MESSAGE_TIMETABLE_READ_ONLY);
        assertCommandException("class enrol 1 sub/Math cost/30 c/Monday1800-1930",
                LogicManager.MESSAGE_TIMETABLE_READ_ONLY);
        assertCommandException("delete 1", LogicManager.MESSAGE_TIMETABLE_READ_ONLY);
        assertCommandException("edit 1 n/Edited", LogicManager.MESSAGE_TIMETABLE_READ_ONLY);
        assertCommandException("add n/Alice p/91234567 e/alice@example.com a/Address",
                LogicManager.MESSAGE_TIMETABLE_READ_ONLY);

        assertEquals(HelpCommand.SHOWING_HELP_MESSAGE,
                logic.execute(HelpCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(PrivacyCommand.MESSAGE_PRIVACY_MODE_ENABLED,
                logic.execute("privacy on").getFeedbackToUser());
        assertEquals(ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT,
                logic.execute(ExitCommand.COMMAND_WORD).getFeedbackToUser());

        CommandResult classListResult = logic.execute("class list");
        assertEquals(CommandResult.ViewChange.CLASS_LIST, classListResult.getViewChange());
        assertEquals(PrivacyCommand.MESSAGE_PRIVACY_MODE_DISABLED,
                logic.execute("privacy off").getFeedbackToUser());

        CommandResult listResult = logic.execute(ListCommand.COMMAND_WORD);
        assertEquals(CommandResult.ViewChange.PERSON_LIST, listResult.getViewChange());
        assertEquals(ClearCommand.MESSAGE_SUCCESS, logic.execute("clear").getFeedbackToUser());
    }

    @Test
    public void execute_classListMode_onlyReadOnlyNavigationCommandsAllowed() throws Exception {
        CommandResult classListResult = logic.execute("class list");
        assertEquals(CommandResult.ViewChange.CLASS_LIST, classListResult.getViewChange());

        assertCommandException("clear", "Class list is read-only. Use \"list\" to return to the student view. "
                + "Only list, timetable, class list, privacy, help, and exit are available.");
        assertEquals(HelpCommand.SHOWING_HELP_MESSAGE,
                logic.execute(HelpCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(ExitCommand.MESSAGE_EXIT_ACKNOWLEDGEMENT,
                logic.execute(ExitCommand.COMMAND_WORD).getFeedbackToUser());
        assertEquals(CommandResult.ViewChange.CLASS_LIST, logic.execute("class list").getViewChange());
        assertEquals(CommandResult.ViewChange.TIMETABLE, logic.execute("timetable").getViewChange());
        assertEquals(CommandResult.ViewChange.PERSON_LIST, logic.execute("list").getViewChange());
    }

    @Test
    public void accessors_delegateToModel() {
        GuiSettings settings = new GuiSettings(800, 600, 10, 20);
        logic.setGuiSettings(settings);

        assertEquals(settings, logic.getGuiSettings());
        assertEquals(model.getTuitionClassList(), logic.getTuitionClassList());
    }

    @Test
    public void getCurrentDate_fixedClock_returnsClockDate() {
        Clock thursdayClock = Clock.fixed(
                Instant.parse("2026-10-08T04:00:00Z"), ZoneOffset.UTC);
        logic = new LogicManager(model, storage, thursdayClock);

        assertEquals(LocalDate.of(2026, 10, 8), logic.getCurrentDate());
    }

    @Test
    public void getTimetable_anchorDate_usesRequestedWeekRatherThanClockDate() {
        Lesson fridayLesson = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Friday1800-1930"));
        model.addPerson(new PersonBuilder().withLessons(fridayLesson).build());
        Clock thursdayClock = Clock.fixed(
                Instant.parse("2026-10-08T04:00:00Z"), ZoneOffset.UTC);
        logic = new LogicManager(model, storage, thursdayClock);

        List<TimetableDay> timetable =
                logic.getTimetable(LocalDate.of(2026, 10, 15));

        assertEquals(LocalDate.of(2026, 10, 11), timetable.get(0).getDate());
        assertEquals(1, timetable.get(5).getEntries().size());
    }

    @Test
    public void timetableQueries_doNotSaveAddressBook() {
        Path addressBookPath = storage.getAddressBookFilePath();

        logic.getCurrentDate();
        logic.getTimetable(LocalDate.of(2026, 10, 20));

        assertFalse(Files.exists(addressBookPath));
    }

    /**
     * Executes the command and confirms that
     * - no exceptions are thrown <br>
     * - the feedback message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandSuccess(String inputCommand, String expectedMessage,
            Model expectedModel) throws CommandException, ParseException {
        CommandResult result = logic.execute(inputCommand);
        assertEquals(expectedMessage, result.getFeedbackToUser());
        assertEquals(expectedModel, model);
    }

    /**
     * Executes the command, confirms that a ParseException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertParseException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, ParseException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that a CommandException is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandException(String inputCommand, String expectedMessage) {
        assertCommandFailure(inputCommand, CommandException.class, expectedMessage);
    }

    /**
     * Executes the command, confirms that the exception is thrown and that the result message is correct.
     * @see #assertCommandFailure(String, Class, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage) {
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs());
        assertCommandFailure(inputCommand, expectedException, expectedMessage, expectedModel);
    }

    /**
     * Executes the command and confirms that
     * - the {@code expectedException} is thrown <br>
     * - the resulting error message is equal to {@code expectedMessage} <br>
     * - the internal model manager state is the same as that in {@code expectedModel} <br>
     * @see #assertCommandSuccess(String, String, Model)
     */
    private void assertCommandFailure(String inputCommand, Class<? extends Throwable> expectedException,
            String expectedMessage, Model expectedModel) {
        assertThrows(expectedException, expectedMessage, () -> logic.execute(inputCommand));
        assertEquals(expectedModel, model);
    }

    /**
     * Tests the Logic component's handling of an {@code IOException} thrown by the Storage component.
     *
     * @param e the exception to be thrown by the Storage component
     * @param expectedMessage the message expected inside exception thrown by the Logic component
     */
    private void assertCommandFailureForExceptionFromStorage(IOException e, String expectedMessage) {
        Path prefPath = temporaryFolder.resolve("ExceptionUserPrefs.json");

        // Inject LogicManager with a JsonAddressBookStorage that throws the IOException e when saving
        JsonAddressBookStorage addressBookStorage = new JsonAddressBookStorage(prefPath) {
            @Override
            public void saveAddressBook(ReadOnlyAddressBook addressBook) throws IOException {
                throw e;
            }
        };

        JsonUserPrefsStorage userPrefsStorage =
                new JsonUserPrefsStorage(temporaryFolder.resolve("ExceptionUserPrefs.json"));
        StorageManager storage = new StorageManager(addressBookStorage, userPrefsStorage);

        logic = new LogicManager(model, storage);

        // Triggers the saveAddressBook method by executing an add command
        String addCommand = AddCommand.COMMAND_WORD + NAME_DESC_AMY + PHONE_DESC_AMY
                + EMAIL_DESC_AMY + ADDRESS_DESC_AMY;
        Model expectedModel = new ModelManager(model.getAddressBook(), new UserPrefs(model.getUserPrefs()));
        assertCommandFailure(addCommand, CommandException.class, expectedMessage, expectedModel);
    }

    private void assertPaymentState(Person person, PaymentStatus expectedStatus, String expectedBalance) {
        Enrollment enrollment = person.getEnrollments().stream().findFirst().orElseThrow();
        assertEquals(expectedStatus, enrollment.getPaymentStatus());
        assertEquals(expectedBalance, person.getOutstandingBalance().toPlainString());
    }
}
