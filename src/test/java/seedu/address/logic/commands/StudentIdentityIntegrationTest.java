package seedu.address.logic.commands;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.GuardianContact;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.testutil.PersonBuilder;

/** Regression checks for normalized duplicate detection and guardian-name persistence. */
public class StudentIdentityIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    private final AddressBookParser parser = new AddressBookParser();

    @Test
    public void add_nameVariants_rejectedWithoutCreatingStudentOrClass() throws Exception {
        ModelManager model = new ModelManager();
        execute(model, "add n/Alpha Nearmatch p/81111111 e/first@example.com a/One");
        AddressBook before = new AddressBook(model.getAddressBook());
        for (String name : List.of("alpha nearmatch", "Alpha  Nearmatch", "ALPHA   NEARMATCH")) {
            CommandException error = assertThrows(CommandException.class, () -> execute(model,
                    "add n/" + name + " p/83333333 e/other@example.com a/Two"
                            + " sub/Math cost/30 c/Thursday1000-1100"));
            assertEquals(AddCommand.MESSAGE_DUPLICATE_PERSON, error.getMessage());
            assertEquals(before, model.getAddressBook());
            assertEquals(before.getNextClassSequence(), model.getAddressBook().getNextClassSequence());
        }
    }

    @Test
    public void edit_filteredNameConflict_rejectedButOwnDisplaySpellingCanChange() throws Exception {
        ModelManager model = new ModelManager();
        execute(model, "add n/Alpha Nearmatch p/81111111 e/first@example.com a/One");
        execute(model, "add n/Beta Student p/82222222 e/second@example.com a/Two");
        execute(model, "find Beta");
        AddressBook before = new AddressBook(model.getAddressBook());
        CommandException error = assertThrows(CommandException.class, () -> execute(model,
                "edit 1 n/alpha  nearmatch"));
        assertEquals(EditCommand.MESSAGE_DUPLICATE_PERSON, error.getMessage());
        assertEquals(before, model.getAddressBook());
        assertEquals("Beta Student", model.getFilteredPersonList().getFirst().getName().fullName);

        var studentId = model.getFilteredPersonList().getFirst().getStudentId();
        execute(model, "edit 1 n/BETA  STUDENT");
        var edited = model.getAddressBook().getPersonList().get(1);
        assertEquals("BETA  STUDENT", edited.getName().fullName);
        assertEquals(studentId, edited.getStudentId());
        assertEquals("82222222", edited.getPhone().value);
    }

    @Test
    public void load_existingNameVariants_preservesBothAndAllowsContactUpdates() throws Exception {
        AddressBook existing = new AddressBook();
        var first = new PersonBuilder().withName("Alpha Nearmatch").build();
        var second = new PersonBuilder().withName("alpha  nearmatch").build();
        existing.addPerson(first);
        existing.addPerson(second);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("students.json"));
        storage.saveAddressBook(existing);
        ModelManager restored = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(existing, restored.getAddressBook());

        execute(restored, "edit 1 p/89999999");
        assertEquals(first.getStudentId(), restored.getAddressBook().getPersonList().getFirst().getStudentId());
        assertEquals(second, restored.getAddressBook().getPersonList().get(1));
        assertThrows(CommandException.class, () -> execute(restored,
                "add n/ALPHA NEARMATCH p/83333333 e/third@example.com a/Three"));
        storage.saveAddressBook(restored.getAddressBook());
        assertEquals(restored.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void add_punctuatedGuardianName_roundTripsWithoutOtherGuardianFields() throws Exception {
        ModelManager model = new ModelManager();
        execute(model, "add n/Guardian Symbol Student p/87777777 e/student@example.com a/Test"
                + " gn/Mary-Anne O'Connor");
        GuardianContact expected = new GuardianContact("Mary-Anne O'Connor", "", "");
        assertEquals(expected, model.getAddressBook().getPersonList().getFirst().getGuardianContact());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("guardians.json"));
        storage.saveAddressBook(model.getAddressBook());
        assertEquals(expected, storage.readAddressBook().orElseThrow().getPersonList().getFirst().getGuardianContact());
    }

    private void execute(ModelManager model, String command) throws Exception {
        parser.parseCommand(command).execute(model);
    }
}
