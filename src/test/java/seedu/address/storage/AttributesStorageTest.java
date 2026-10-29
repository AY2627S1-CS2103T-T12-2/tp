package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.person.GuardianContact;

/** Verifies real JSON persistence and compatibility for both kinds of attributes. */
public class AttributesStorageTest {

    @TempDir
    public Path temporaryFolder;

    @Test
    public void saveAndReload_attributesAndPayments_roundTripWithoutLoss() throws Exception {
        ModelManager model = populatedModel();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("students.json"));
        storage.saveAddressBook(model.getAddressBook());
        assertEquals(model.getAddressBook(), storage.readAddressBook().orElseThrow());
        ModelManager restored = new ModelManager(storage.readAddressBook().orElseThrow(), model.getUserPrefs());
        new AddressBookParser().parseCommand("tag 1 exam-prep").execute(restored);
        storage.saveAddressBook(restored.getAddressBook());
        assertEquals(restored.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void load_oldDataWithoutNewFields_usesEmptyAttributes() throws Exception {
        String json = """
                {"persons":[{"name":"Alice","phone":"91234567","email":"alice@example.com",
                "address":"One","tags":[],"lessons":[{"subject":"Math","cost":"30",
                "timing":"Monday1800-1930","paymentStatus":"PAID"}]}]}
                """;
        AddressBook result = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        assertEquals(GuardianContact.EMPTY, result.getPersonList().getFirst().getGuardianContact());
        var lesson = result.getPersonList().getFirst().getLessons().iterator().next();
        assertEquals(ClassDetails.EMPTY, lesson.getDetails());
        assertEquals(PaymentStatus.PAID, lesson.getPaymentStatus());
    }

    @Test
    public void load_invalidNewFields_rejectsFile() throws Exception {
        String valid = JsonUtil.toJsonString(new JsonSerializableAddressBook(populatedModel().getAddressBook()));
        for (String invalid : List.of(valid.replace("98765432", "abc"),
                valid.replace("mary@example.com", "invalid"), valid.replace("Mary Tan", "@Mary"),
                valid.replace("https://example.com/math", "javascript:alert(1)"),
                valid.replace("Room 2", "x".repeat(501)),
                valid.replace("ONLINE", "HYBRID"), valid.replace("ONLINE", "OFFLINE"))) {
            var data = JsonUtil.fromJsonString(invalid, JsonSerializableAddressBook.class);
            assertThrows(IllegalValueException.class, data::toModelType);
            Path file = temporaryFolder.resolve("invalid.json");
            JsonUtil.saveJsonFile(data, file);
            assertThrows(DataLoadingException.class, () -> new JsonAddressBookStorage(file).readAddressBook());
        }
    }

    @Test
    public void load_conflictingDetailsForSameClass_rejectsFile() throws Exception {
        ModelManager model = populatedModel();
        var bob = model.getAddressBook().getPersonList().get(1);
        var originalLesson = bob.getLessons().iterator().next();
        AddressBook inconsistent = new AddressBook(model.getAddressBook());
        inconsistent.setPerson(bob,
                bob.withLessons(java.util.Set.of(originalLesson.withDetails(ClassDetails.EMPTY))));
        JsonSerializableAddressBook data = new JsonSerializableAddressBook(inconsistent);
        var exception = assertThrows(IllegalValueException.class, data::toModelType);
        assertEquals(JsonSerializableAddressBook.MESSAGE_CLASS_DETAILS_CONFLICT, exception.getMessage());
    }

    @Test
    public void quotedDetailsAndReorderedGuardian_survivePaymentEditAndRestart() throws Exception {
        ModelManager model = new ModelManager();
        AddressBookParser parser = new AddressBookParser();
        parser.parseCommand("add notes/\"Review gn/Mary sub/section 2\" location/\"Room sub/section 2\""
                + " gp/99999999 n/Alice p/91234567 e/alice@example.com a/One"
                + " sub/Math cost/35 c/Monday1800-1930 mode/online").execute(model);
        parser.parseCommand("pin 1").execute(model);
        parser.parseCommand("find sub/Math").execute(model);
        parser.parseCommand("class payment 1 id/C001 status/paid").execute(model);
        parser.parseCommand("edit 1 p/91111111").execute(model);
        parser.parseCommand("class edit id/C001 sub/Physics cost/50 c/Tuesday1800-1930").execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("quoted.json"));
        storage.saveAddressBook(model.getAddressBook());
        ModelManager restored = new ModelManager(storage.readAddressBook().orElseThrow(), model.getUserPrefs());
        assertEquals(model.getAddressBook(), restored.getAddressBook());
        var person = restored.getAddressBook().getPersonList().getFirst();
        var lesson = person.getLessons().iterator().next();
        assertEquals("99999999", person.getGuardianContact().phone());
        assertEquals("91111111", person.getPhone().value);
        assertEquals(PaymentStatus.PAID, lesson.getPaymentStatus());
        assertEquals("35", lesson.getCost().toString());
        assertEquals("Review gn/Mary sub/section 2", lesson.getDetails().notes());
        assertEquals("Room sub/section 2", lesson.getDetails().location());
        parser.parseCommand("find sub/Physics").execute(restored);
        assertEquals(1, restored.getFilteredPersonList().size());
    }

    private ModelManager populatedModel() throws Exception {
        ModelManager model = new ModelManager();
        AddressBookParser parser = new AddressBookParser();
        for (String command : List.of(
                "add n/Alice p/91234567 e/alice@example.com a/One gn/Mary Tan gp/98765432 ge/mary@example.com"
                        + " sub/Math cost/30 c/Monday1800-1930 mode/online link/https://example.com/math"
                        + " location/Room 2 notes/Bring notes",
                "add n/Bob p/81234567 e/bob@example.com a/Two sub/Math cost/30 c/Monday1800-1930",
                "class payment 1 sub/Math c/Monday1800-1930 status/paid")) {
            parser.parseCommand(command).execute(model);
        }
        return model;
    }
}
