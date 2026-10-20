package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.model.AddressBook;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.ClassMode;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;

/** Protects attributes when migrating both pre-catalogue v1.4 files and PR 88 class files. */
public class ClassAttributesMigrationTest {

    private static final String CATALOGUE_JSON = """
            {"classes":[{"id":"C004","subject":"Math","cost":"30","timing":"Saturday1800-1930",
            "mode":"online","meetingLink":"https://example.com/math","notes":"Bring notes"}],
            "persons":[{"name":"Alice","phone":"91234567","email":"alice@example.com","address":"One",
            "guardianName":"Mary","guardianPhone":"99999999","tags":[],"lessons":[{"classId":"C004",
            "subject":"Math","cost":"30","timing":"Saturday1800-1930","paymentStatus":"PAID"}]}]}
            """;

    @TempDir
    public Path temporaryFolder;

    @Test
    public void migrateOldV14File_keepsOnlineDetailsGuardianAndPayment() throws Exception {
        String json = """
                {"persons":[{"name":"Alice","phone":"91234567","email":"alice@example.com","address":"One",
                "guardianName":"Mary","guardianPhone":"99999999","tags":[],"lessons":[{"subject":"Math",
                "cost":"30","timing":"Saturday1800-1930","paymentStatus":"PAID","mode":"ONLINE",
                "meetingLink":"https://example.com/math","location":"Room 2","notes":"Bring notes"}]}]}
                """;
        AddressBook loaded = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        ModelManager restarted = new ModelManager(loaded, new UserPrefs());
        var person = restarted.getAddressBook().getPersonList().getFirst();
        var lesson = person.getLessons().iterator().next();
        assertEquals("99999999", person.getGuardianContact().phone());
        assertEquals(PaymentStatus.PAID, lesson.getPaymentStatus());
        assertEquals(new ClassId("C001"), lesson.getClassId());
        assertEquals(ClassMode.ONLINE, lesson.getDetails().mode());
        assertEquals("https://example.com/math", lesson.getDetails().meetingLink());
        assertEquals("Room 2", lesson.getDetails().location());
        assertEquals(lesson.getDetails(), restarted.getTuitionClassList().getFirst().getDetails());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("migrated.json"));
        storage.saveAddressBook(restarted.getAddressBook());
        assertEquals(restarted.getAddressBook(), storage.readAddressBook().orElseThrow());
    }

    @Test
    public void missingEnrolmentFields_inheritCentralClassDetailsAcrossRestart() throws Exception {
        AddressBook loaded = JsonUtil.fromJsonString(CATALOGUE_JSON, JsonSerializableAddressBook.class).toModelType();
        var lesson = loaded.getPersonList().getFirst().getLessons().iterator().next();
        assertEquals(new ClassId("C004"), lesson.getClassId());
        assertEquals(ClassMode.ONLINE, lesson.getDetails().mode());
        assertEquals("https://example.com/math", lesson.getDetails().meetingLink());
        assertEquals("99999999", loaded.getPersonList().getFirst().getGuardianContact().phone());
        assertEquals(PaymentStatus.PAID, lesson.getPaymentStatus());
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("central.json"));
        storage.saveAddressBook(loaded);
        assertEquals(loaded, storage.readAddressBook().orElseThrow());
    }

    @Test
    public void explicitlyConflictingEnrolmentFields_rejectsData() throws Exception {
        String invalid = CATALOGUE_JSON.replace("\"paymentStatus\":\"PAID\"",
                "\"paymentStatus\":\"PAID\",\"mode\":\"offline\"");
        var data = JsonUtil.fromJsonString(invalid, JsonSerializableAddressBook.class);
        assertThrows(IllegalValueException.class, data::toModelType);
    }

    @Test
    public void classWithoutStudents_keepsAttributesInStorage() throws Exception {
        AddressBook book = new AddressBook();
        ClassDetails details = new ClassDetails(ClassMode.ONLINE, "https://example.com/math", "", "Bring notes");
        book.addTuitionClass(new TuitionClass(new ClassId("C003"), new Subject("Math"), new Cost("30"),
                new LessonTiming("Saturday1800-1930"), details));
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("empty-class.json"));
        storage.saveAddressBook(book);
        var restored = storage.readAddressBook().orElseThrow();
        assertTrue(restored.getPersonList().isEmpty());
        assertEquals(book, restored);
        assertEquals(details, restored.getTuitionClassList().getFirst().getDetails());
        assertEquals(4, restored.getNextClassSequence());
    }
}
