package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.List;

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
    public void oneOffAttributesAndPinState_surviveJsonRoundTripAndNormalisation() throws Exception {
        ModelManager model = new ModelManager();
        var parser = new seedu.address.logic.parser.AddressBookParser();
        parser.parseCommand("add n/Alice p/91234567 e/alice@example.com a/One gn/Mary gp/99999999"
                + " sub/Math cost/30 c/2026-10-24T1800-1930 mode/online link/https://example.com/math")
                .execute(model);
        parser.parseCommand("pin 1").execute(model);
        parser.parseCommand("class payment 1 id/C001 status/paid").execute(model);
        JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("one-off.json"));
        storage.saveAddressBook(model.getAddressBook());
        var reloaded = storage.readAddressBook().orElseThrow();
        assertEquals(model.getAddressBook(), reloaded);
        var person = reloaded.getPersonList().getFirst();
        assertTrue(person.isPinned());
        assertEquals("99999999", person.getGuardianContact().phone());
        assertEquals("2026-10-24T1800-1930", person.getLessons().iterator().next().getSchedule().toStorageString());
        assertEquals(reloaded, new ModelManager(reloaded, new UserPrefs()).getAddressBook());
    }

    @Test
    public void oldScheduleProperty_isReadAndRewrittenUsingCanonicalTiming() throws Exception {
        String json = CATALOGUE_JSON.replace("\"timing\":\"Saturday1800-1930\",\"paymentStatus\"",
                "\"schedule\":\"Saturday1800-1930\",\"paymentStatus\"");
        AddressBook loaded = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        String saved = JsonUtil.toJsonString(new JsonSerializableAddressBook(loaded));
        assertTrue(saved.contains("\"timing\""));
        assertEquals(loaded, JsonUtil.fromJsonString(saved, JsonSerializableAddressBook.class).toModelType());
    }

    @Test
    public void emptyUndescribedCatalogueClass_acceptsExplicitLegacyDetails() throws Exception {
        String json = """
                {"classes":[{"id":"C004","subject":"Math","cost":"30","timing":"Saturday1800-1930"}],
                "persons":[{"name":"Alice","phone":"91234567","email":"alice@example.com","address":"One",
                "tags":[],"lessons":[{"classId":"C004","subject":"Math","cost":"30",
                "timing":"Saturday1800-1930","paymentStatus":"PAID","meetingLink":"https://example.com/math"}]}]}
                """;
        AddressBook loaded = JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
        assertEquals(ClassMode.ONLINE, loaded.getTuitionClassList().getFirst().getDetails().mode());
        assertEquals("https://example.com/math", loaded.getTuitionClassList().getFirst().getDetails().meetingLink());
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

    @Test
    public void omittedLegacyDetails_inheritExplicitDetailsInEitherStudentOrder() throws Exception {
        String alice = legacyStudent("Alice", "");
        String bob = legacyStudent("Bob", ",\"mode\":\"ONLINE\",\"meetingLink\":\"https://example.com/math\","
                + "\"notes\":\"Review\"");
        for (String students : List.of(alice + "," + bob, bob + "," + alice)) {
            AddressBook loaded = JsonUtil.fromJsonString("{\"persons\":[" + students + "]}",
                    JsonSerializableAddressBook.class).toModelType();
            assertEquals(1, loaded.getTuitionClassList().size());
            ClassDetails expected = new ClassDetails(ClassMode.ONLINE, "https://example.com/math", "", "Review");
            for (var person : loaded.getPersonList()) {
                assertEquals(expected, person.getLessons().iterator().next().getDetails());
                assertEquals(PaymentStatus.PAID, person.getLessons().iterator().next().getPaymentStatus());
            }
            JsonAddressBookStorage storage = new JsonAddressBookStorage(temporaryFolder.resolve("ordered.json"));
            storage.saveAddressBook(loaded);
            assertEquals(loaded, storage.readAddressBook().orElseThrow());
        }
    }

    @Test
    public void explicitlyConflictingLegacyDetails_rejectedInEitherStudentOrder() throws Exception {
        String alice = legacyStudent("Alice", ",\"mode\":\"OFFLINE\"");
        String bob = legacyStudent("Bob", ",\"mode\":\"ONLINE\",\"notes\":\"Review\"");
        for (String students : List.of(alice + "," + bob, bob + "," + alice)) {
            var data = JsonUtil.fromJsonString("{\"persons\":[" + students + "]}", JsonSerializableAddressBook.class);
            assertEquals(JsonSerializableAddressBook.MESSAGE_CLASS_DETAILS_CONFLICT,
                    assertThrows(IllegalValueException.class, data::toModelType).getMessage());
        }
    }

    private String legacyStudent(String name, String details) {
        return """
                {"name":"%s","phone":"91234567","email":"student@example.com","address":"One",
                "tags":[],"lessons":[{"subject":"Math","cost":"30","timing":"Monday1800-1930",
                "paymentStatus":"PAID"%s}]}
                """.formatted(name, details);
    }
}
