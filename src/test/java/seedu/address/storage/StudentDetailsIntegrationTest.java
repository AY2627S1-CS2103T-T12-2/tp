package seedu.address.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.commons.util.JsonUtil;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.AddCommand;
import seedu.address.logic.commands.EditCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.AddCommandParser;
import seedu.address.logic.parser.AddressBookParser;
import seedu.address.logic.parser.ParserUtil;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.AddressBook;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.person.Cost;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/**
 * Tests student details across parsing, commands, the model, editing and storage.
 */
public class StudentDetailsIntegrationTest {
    private static final String BASE = " n/John Davis p/91234567 e/johndavis@gmail.com a/Clementi";
    private static final String DETAILS = " sub/Math cost/30 c/Monday1800-1900";

    @TempDir
    public Path tempDir;

    private Person addTo(Model model, String arguments) throws Exception {
        new AddressBookParser().parseCommand("add" + arguments).execute(model);
        return model.getFilteredPersonList().get(0);
    }

    @Test
    public void add_allStudentDetails_storedInModel() throws Exception {
        Person person = addTo(new ModelManager(), BASE + DETAILS);
        assertEquals(1, person.getEnrollments().size());
        var enrolment = person.getEnrollments().iterator().next();
        assertEquals("Math", enrolment.getTuitionClass().getSubject().toString());
        assertEquals("30", enrolment.getAgreedRate().toString());
        assertEquals("Monday1800-1900", enrolment.getTuitionClass().getSchedule().toStorageString());
        assertTrue(person.getSubjects().isEmpty());
        assertTrue(person.getCost().isEmpty());
        assertTrue(person.getLesson().isEmpty());
    }

    @Test
    public void add_optionalDetailsOmitted_accepted() throws Exception {
        Person basic = addTo(new ModelManager(), BASE);
        assertTrue(basic.getSubjects().isEmpty());
        assertTrue(basic.getCost().isEmpty());
        assertTrue(basic.getLesson().isEmpty());

        assertTrue(basic.getEnrollments().isEmpty());
    }

    @Test
    public void add_reorderedFields_accepted() throws Exception {
        Person person = addTo(new ModelManager(),
                " cost/30 sub/Math n/John Davis c/Monday1800-1900 a/Clementi"
                        + " e/johndavis@gmail.com p/91234567");
        var enrolment = person.getEnrollments().iterator().next();
        assertEquals("Math", enrolment.getTuitionClass().getSubject().toString());
        assertEquals("30", enrolment.getAgreedRate().toString());
        assertEquals("Monday1800-1900", enrolment.getTuitionClass().getSchedule().toStorageString());
    }

    @Test
    public void parserUtil_trimsValuesAndRejectsInvalidInput() throws Exception {
        assertEquals(new Subject("Computer Science"), ParserUtil.parseSubject("  Computer Science  "));
        assertEquals(Set.of(new Subject("Math")), ParserUtil.parseSubjects(List.of("Math", " Math ")));
        assertEquals(new Cost("30"), ParserUtil.parseCost(" 30 "));
        assertEquals(new Lesson("Monday1800"), ParserUtil.parseLesson(" Monday1800 "));
        assertEquals(Subject.MESSAGE_CONSTRAINTS,
                assertThrows(ParseException.class, () -> ParserUtil.parseSubject(" ")).getMessage());
        assertEquals(Cost.MESSAGE_CONSTRAINTS,
                assertThrows(ParseException.class, () -> ParserUtil.parseCost("0")).getMessage());
        assertEquals(Lesson.MESSAGE_CONSTRAINTS,
                assertThrows(ParseException.class, () -> ParserUtil.parseLesson("Monday2400")).getMessage());
    }

    @Test
    public void add_invalidDetailsRejected_modelUnchanged() {
        for (String invalid : List.of(" sub/", " sub/Math!", " sub/Math sub/Physics,",
                " cost/", " cost/0", " cost/-30", " cost/30.50", " cost/abc",
                " c/", " c/Monday2400", " c/Monday1860", " c/Funday1800")) {
            Model model = new ModelManager();
            assertThrows(ParseException.class, () -> addTo(model, BASE + invalid), invalid);
            assertTrue(model.getAddressBook().getPersonList().isEmpty(), invalid);
        }
    }

    @Test
    public void add_repeatedSingleValuedFields_rejected() {
        AddCommandParser parser = new AddCommandParser();
        assertThrows(ParseException.class, () -> parser.parse(BASE + " cost/30 cost/40"));
        assertThrows(ParseException.class, () -> parser.parse(BASE + " c/Monday1800 c/Tuesday1900"));
        assertThrows(ParseException.class, () -> parser.parse(BASE + DETAILS + " sub/Math"));
    }

    @Test
    public void add_duplicateNameWithDifferentDetails_rejected() throws Exception {
        Model model = new ModelManager();
        Person original = addTo(model, BASE + DETAILS);
        CommandException error = assertThrows(CommandException.class, () ->
                addTo(model, BASE + " sub/Physics cost/40 c/Tuesday1900-2000"));
        assertEquals(AddCommand.MESSAGE_DUPLICATE_PERSON, error.getMessage());
        assertEquals(List.of(original), model.getAddressBook().getPersonList());
    }

    @Test
    public void editContactDetails_preservesStudentDetails() throws Exception {
        Model model = new ModelManager();
        Person original = addTo(model, BASE + DETAILS);
        EditCommand.EditPersonDescriptor changes = new EditCommand.EditPersonDescriptor();
        changes.setPhone(new Phone("98765432"));
        new EditCommand(Index.fromOneBased(1), changes).execute(model);
        Person edited = model.getFilteredPersonList().get(0);
        assertEquals(new Phone("98765432"), edited.getPhone());
        assertEquals(original.getEnrollments(), edited.getEnrollments());
        assertEquals(original.getLessons(), edited.getLessons());
    }

    @Test
    public void tagAndUntag_preserveStudentDetails() throws Exception {
        Model model = new ModelManager();
        Person original = addTo(model, BASE + " t/friends" + DETAILS);

        new AddressBookParser().parseCommand("tag 1 exam-prep").execute(model);
        Person tagged = model.getFilteredPersonList().get(0);
        assertEquals(Set.of(new Tag("friends"), new Tag("exam-prep")), tagged.getTags());
        assertEquals(original.getEnrollments(), tagged.getEnrollments());
        assertEquals(original.getLessons(), tagged.getLessons());

        new AddressBookParser().parseCommand("untag 1 friends").execute(model);
        Person untagged = model.getFilteredPersonList().get(0);
        assertEquals(Set.of(new Tag("exam-prep")), untagged.getTags());
        assertEquals(original.getEnrollments(), untagged.getEnrollments());
        assertEquals(original.getLessons(), untagged.getLessons());

        new AddressBookParser().parseCommand("untag 1 -all").execute(model);
        Person withoutTags = model.getFilteredPersonList().get(0);
        assertTrue(withoutTags.getTags().isEmpty());
        assertEquals(original.getEnrollments(), withoutTags.getEnrollments());
        assertEquals(original.getLessons(), withoutTags.getLessons());
    }

    @Test
    public void saveAndReload_retainsScheduledAndBasicStudents() throws Exception {
        for (String details : List.of(DETAILS, "")) {
            Model model = new ModelManager();
            Person original = addTo(model, BASE + details);
            Path file = tempDir.resolve("students.json");
            new JsonAddressBookStorage(file).saveAddressBook(model.getAddressBook());
            Person restored = new JsonAddressBookStorage(file).readAddressBook().orElseThrow()
                    .getPersonList().get(0);
            assertEquals(original, restored);
            assertEquals(original.hashCode(), restored.hashCode());
        }
    }

    @Test
    public void loadLegacyJson_partialClassFields_surviveSaveAndReload() throws Exception {
        for (JsonAdaptedPerson record : List.of(adapted(List.of("Math"), null, null),
                adapted(List.of(), "20", null), adapted(List.of("English"), "20", null))) {
            var original = new JsonSerializableAddressBook(List.of(record)).toModelType();
            Path file = tempDir.resolve("legacy-students.json");
            JsonAddressBookStorage storage = new JsonAddressBookStorage(file);
            storage.saveAddressBook(original);
            var restored = storage.readAddressBook().orElseThrow();
            assertEquals(original, restored);
            Person student = restored.getPersonList().get(0);
            assertTrue(student.getEnrollments().isEmpty());
            assertEquals(record.toModelType().getSubjects(), student.getSubjects());
            assertEquals(record.toModelType().getCost(), student.getCost());
        }
    }

    @Test
    public void unenrolMigratedLegacyStudent_saveAndReload_doesNotRestoreEnrollment() throws Exception {
        Model model = new ModelManager(loadCompleteLegacyStudent(), new UserPrefs());
        new AddressBookParser().parseCommand("class unenrol 1 id/C001").execute(model);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(tempDir.resolve("unenrolled.json"));
        storage.saveAddressBook(model.getAddressBook());
        var restored = storage.readAddressBook().orElseThrow();

        assertTrue(restored.getPersonList().getFirst().getEnrollments().isEmpty());
        assertEquals(1, restored.getTuitionClassList().size());
    }

    @Test
    public void deleteMigratedLegacyClass_saveAndReload_doesNotRestoreClass() throws Exception {
        Model model = new ModelManager(loadCompleteLegacyStudent(), new UserPrefs());
        new AddressBookParser().parseCommand("class delete id/C001").execute(model);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(tempDir.resolve("deleted-class.json"));
        storage.saveAddressBook(model.getAddressBook());
        var restored = storage.readAddressBook().orElseThrow();

        assertTrue(restored.getPersonList().getFirst().getEnrollments().isEmpty());
        assertTrue(restored.getTuitionClassList().isEmpty());
    }

    @Test
    public void unenrolPreviouslySavedMigration_saveAndReload_doesNotRestoreEnrollment() throws Exception {
        Model model = new ModelManager(loadPreviouslySavedMigration(), new UserPrefs());
        new AddressBookParser().parseCommand("class unenrol 1 id/C001").execute(model);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(tempDir.resolve("previously-migrated.json"));
        storage.saveAddressBook(model.getAddressBook());
        var restored = storage.readAddressBook().orElseThrow();

        assertTrue(restored.getPersonList().getFirst().getEnrollments().isEmpty());
        assertEquals(1, restored.getTuitionClassList().size());
    }

    @Test
    public void deletePreviouslySavedMigratedClass_saveAndReload_doesNotRestoreClass() throws Exception {
        Model model = new ModelManager(loadPreviouslySavedMigration(), new UserPrefs());
        new AddressBookParser().parseCommand("class delete id/C001").execute(model);

        JsonAddressBookStorage storage = new JsonAddressBookStorage(tempDir.resolve("previously-deleted-class.json"));
        storage.saveAddressBook(model.getAddressBook());
        var restored = storage.readAddressBook().orElseThrow();

        assertTrue(restored.getPersonList().getFirst().getEnrollments().isEmpty());
        assertTrue(restored.getTuitionClassList().isEmpty());
    }

    @Test
    public void loadLegacyJson_unmigratableLateLesson_survivesSaveAndReload() throws Exception {
        JsonAdaptedPerson record = adapted(List.of("Math"), "30", "Monday2359");
        var original = new JsonSerializableAddressBook(List.of(record)).toModelType();
        JsonAddressBookStorage storage = new JsonAddressBookStorage(tempDir.resolve("late-lesson.json"));

        storage.saveAddressBook(original);
        var restored = storage.readAddressBook().orElseThrow();

        assertEquals(original, restored);
        assertTrue(restored.getPersonList().getFirst().hasUnmigratedLegacyLesson());
    }

    @Test
    public void previouslySavedMigrationWithUnmigratableLegacyDetails_preservesDetails() throws Exception {
        AddressBook original = loadPreviouslySavedMigration("Monday2359");
        JsonAddressBookStorage storage = new JsonAddressBookStorage(tempDir.resolve("migrated-late-lesson.json"));

        storage.saveAddressBook(original);
        var restored = storage.readAddressBook().orElseThrow();

        assertEquals(original, restored);
        assertEquals(Optional.of(new Lesson("Monday2359")), restored.getPersonList().getFirst().getLesson());
    }

    @Test
    public void loadLegacyJson_withoutStudentDetails_accepted() throws Exception {
        String json = "{\"name\":\"John Davis\",\"phone\":\"91234567\","
                + "\"email\":\"johndavis@gmail.com\",\"address\":\"Clementi\",\"tags\":[]}";
        Person restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
        assertEquals(addTo(new ModelManager(), BASE), restored);
        assertFalse(restored.isPinned());
    }

    @Test
    public void loadJson_withPinnedState_restoresPinnedState() throws Exception {
        String json = "{\"name\":\"John Davis\",\"phone\":\"91234567\","
                + "\"email\":\"johndavis@gmail.com\",\"address\":\"Clementi\",\"tags\":[],"
                + "\"pinned\":true}";

        Person restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();

        assertTrue(restored.isPinned());
    }

    @Test
    public void loadLegacyJson_withCompleteStudentDetails_migratesLesson() throws Exception {
        String json = "{\"name\":\"John Davis\",\"phone\":\"91234567\","
                + "\"email\":\"johndavis@gmail.com\",\"address\":\"Clementi\",\"tags\":[],"
                + "\"subjects\":[\"Math\"],\"cost\":\"30\",\"lesson\":\"Monday1800\"}";

        Person restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();

        assertEquals(Set.of(new seedu.address.model.lesson.Lesson(
                new seedu.address.model.lesson.Subject("Math"),
                new seedu.address.model.lesson.Cost("30"),
                new seedu.address.model.lesson.LessonTiming("Monday1800-1900"))), restored.getLessons());
        assertEquals(Set.of(new Subject("Math")), restored.getSubjects());
        assertEquals(Optional.of(new Cost("30")), restored.getCost());
        assertEquals(Optional.of(new Lesson("Monday1800")), restored.getLesson());
    }

    @Test
    public void loadInvalidStudentDetails_rejected() {
        List<JsonAdaptedPerson> invalidRecords = new ArrayList<>();
        for (List<String> subjects : List.of(List.of("Math!"), List.of(" "), Arrays.asList((String) null))) {
            invalidRecords.add(adapted(subjects, null, null));
        }
        invalidRecords.add(adapted(List.of(), "0", null));
        invalidRecords.add(adapted(List.of(), "-30", null));
        invalidRecords.add(adapted(List.of(), null, "Monday2400"));
        for (JsonAdaptedPerson record : invalidRecords) {
            assertThrows(IllegalValueException.class, record::toModelType);
        }
    }

    @Test
    public void person_copiesSubjectsAndIncludesDetailsInEquality() throws Exception {
        Person basic = addTo(new ModelManager(), BASE);
        Set<Subject> subjects = new HashSet<>(Set.of(new Subject("Math")));
        Person enriched = new Person(basic.getName(), basic.getPhone(), basic.getEmail(),
                basic.getAddress(), basic.getTags(), subjects, Optional.empty(), Optional.empty());
        subjects.clear();
        assertEquals(Set.of(new Subject("Math")), enriched.getSubjects());
        assertThrows(UnsupportedOperationException.class, () -> enriched.getSubjects().clear());
        assertFalse(basic.equals(enriched));
        assertTrue(basic.isSamePerson(enriched));

        Person withCost = new Person(basic.getName(), basic.getPhone(), basic.getEmail(),
                basic.getAddress(), basic.getTags(), Set.of(), Optional.of(new Cost("30")), Optional.empty());
        Person withLesson = new Person(basic.getName(), basic.getPhone(), basic.getEmail(),
                basic.getAddress(), basic.getTags(), Set.of(), Optional.empty(), Optional.of(new Lesson("Monday1800")));
        assertFalse(basic.equals(withCost));
        assertFalse(basic.equals(withLesson));
    }

    @Test
    public void add_returnsStudentSuccessMessage() throws Exception {
        Model model = new ModelManager();
        String feedback = new AddCommandParser().parse(BASE + DETAILS).execute(model).getFeedbackToUser();
        assertEquals(String.format(AddCommand.MESSAGE_SUCCESS,
                Messages.format(model.getFilteredPersonList().get(0))), feedback);
        assertTrue(feedback.startsWith("Student Added:"));
    }

    private JsonAdaptedPerson adapted(List<String> subjects, String cost, String lesson) {
        return new JsonAdaptedPerson("John Davis", "91234567", "johndavis@gmail.com", "Clementi",
                List.of(), subjects, cost, lesson);
    }

    private AddressBook loadCompleteLegacyStudent() throws Exception {
        String json = """
                {"persons":[{"name":"Alice","phone":"91234567","email":"alice@example.com",
                "address":"One","tags":[],"subjects":["Math"],"cost":"30","lesson":"Monday1800"}]}
                """;
        return JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
    }

    private AddressBook loadPreviouslySavedMigration() throws Exception {
        return loadPreviouslySavedMigration("Monday1800");
    }

    private AddressBook loadPreviouslySavedMigration(String legacyLesson) throws Exception {
        String json = """
                {"classes":[{"id":"C001","subject":"Math","cost":"30","timing":"Monday1800-1900"}],
                "persons":[{"name":"Alice","phone":"91234567","email":"alice@example.com","address":"One",
                "tags":[],"studentId":"S-00000000-0000-0000-0000-000000000001",
                "lessons":[{"enrollmentId":"E-00000000-0000-0000-0000-000000000001",
                "studentId":"S-00000000-0000-0000-0000-000000000001","classId":"C001",
                "cost":"30","paymentStatus":"UNPAID"}],
                "subjects":["Math"],"cost":"30","lesson":"%s"}],"nextClassSequence":2}
                """.formatted(legacyLesson);
        return JsonUtil.fromJsonString(json, JsonSerializableAddressBook.class).toModelType();
    }
}
