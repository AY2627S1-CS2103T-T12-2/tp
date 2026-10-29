package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.parser.AddCommandParser;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;

/** Checks that incomplete new class input cannot create partial records or affect saved data. */
public class IncompleteClassInputIntegrationTest {

    private static final String BASE = "add n/Incomplete Student p/95550021 e/incomplete@example.com a/Test";

    @TempDir
    public Path temporaryFolder;

    @Test
    public void execute_partialClassFields_rejectedWithoutChangingModelFilterOrDisk() throws Exception {
        ModelManager model = new ModelManager();
        StorageManager storage = storage();
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("add n/Existing Student p/91111111 e/existing@example.com a/One"
                + " sub/Math cost/30 c/Thursday1000-1100 mode/online link/https://example.com");
        logic.execute("find Existing");
        AddressBook before = new AddressBook(model.getAddressBook());
        var visibleBefore = List.copyOf(logic.getFilteredPersonList());
        String savedBefore = Files.readString(storage.getAddressBookFilePath());
        for (String fields : List.of(" sub/English", " cost/20", " c/Monday1800-1900",
                " sub/English cost/20", " sub/English c/Monday1800-1900", " cost/20 c/Monday1800-1900")) {
            ParseException error = assertThrows(ParseException.class, () -> logic.execute(BASE + fields));
            assertEquals(AddCommandParser.MESSAGE_INCOMPLETE_CLASS, error.getMessage());
            assertEquals(before, model.getAddressBook());
            assertEquals(before.getNextClassSequence(), model.getAddressBook().getNextClassSequence());
            assertEquals(visibleBefore, logic.getFilteredPersonList());
            assertEquals(savedBefore, Files.readString(storage.getAddressBookFilePath()));
        }
        ModelManager restarted = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(before, restarted.getAddressBook());
        assertEquals(1, restarted.getAddressBook().getPersonList().size());
        assertEquals(1, restarted.getTuitionClassList().size());
    }

    @Test
    public void execute_invalidClassValuesOrOrphanedDetails_rejectedWithoutAdditions() throws Exception {
        ModelManager model = new ModelManager();
        StorageManager storage = storage();
        LogicManager logic = new LogicManager(model, storage);
        storage.saveAddressBook(model.getAddressBook());
        logic.execute("list");
        String savedBefore = Files.readString(storage.getAddressBookFilePath());
        for (String fields : List.of(" sub/English cost/20 c/Monday1800", " sub/English cost/20 c/",
                " sub/ cost/20 c/Monday1800-1900", " sub/English cost/ c/Monday1800-1900",
                " sub/English sub/Math cost/20 c/Monday1800-1900",
                " mode/online", " link/https://example.com", " location/Room 2", " notes/Bring notes")) {
            assertThrows(ParseException.class, () -> logic.execute(BASE + fields));
            assertTrue(model.getAddressBook().getPersonList().isEmpty());
            assertTrue(model.getTuitionClassList().isEmpty());
            assertEquals(1, model.getAddressBook().getNextClassSequence());
            assertEquals(savedBefore, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void execute_basicGuardianWeeklyAndOneOffStudents_persistWithExpectedEnrolments() throws Exception {
        ModelManager model = new ModelManager();
        StorageManager storage = storage();
        LogicManager logic = new LogicManager(model, storage);
        logic.execute("add n/Guardian Student p/91111111 e/guardian@example.com a/One gp/98888888");
        logic.execute("add n/Weekly Student p/92222222 e/weekly@example.com a/Two"
                + " sub/Math cost/20.50 c/Thursday1000-1100");
        logic.execute("add n/OneOff Student p/93333333 e/oneoff@example.com a/Three"
                + " sub/English cost/0 c/2026-11-03T1400-1500");
        var persons = model.getAddressBook().getPersonList();
        assertTrue(persons.get(0).getEnrollments().isEmpty());
        assertEquals("98888888", persons.get(0).getGuardianContact().phone());
        assertEquals("20.5", persons.get(1).getOutstandingBalance().toPlainString());
        assertEquals(1, persons.get(2).getEnrollments().size());
        assertEquals(2, model.getTuitionClassList().size());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    private StorageManager storage() {
        return new StorageManager(new JsonAddressBookStorage(temporaryFolder.resolve("students.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
    }
}
