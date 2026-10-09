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
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
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
    private static final String DETAILS = " sub/Math sub/CS2103T cost/30 c/Monday1800";

    @TempDir
    public Path tempDir;

    private Person addTo(Model model, String arguments) throws Exception {
        new AddressBookParser().parseCommand("add" + arguments).execute(model);
        return model.getFilteredPersonList().get(0);
    }

    @Test
    public void add_allStudentDetails_storedInModel() throws Exception {
        Person person = addTo(new ModelManager(), BASE + DETAILS);
        assertEquals(Set.of(new Subject("Math"), new Subject("CS2103T")), person.getSubjects());
        assertEquals(Optional.of(new Cost("30")), person.getCost());
        assertEquals(Optional.of(new Lesson("Monday1800")), person.getLesson());
    }

    @Test
    public void add_optionalDetailsOmitted_accepted() throws Exception {
        Person basic = addTo(new ModelManager(), BASE);
        assertTrue(basic.getSubjects().isEmpty());
        assertTrue(basic.getCost().isEmpty());
        assertTrue(basic.getLesson().isEmpty());

        Person withoutCost = addTo(new ModelManager(), BASE + " sub/Science c/Sunday0000");
        assertEquals(Set.of(new Subject("Science")), withoutCost.getSubjects());
        assertTrue(withoutCost.getCost().isEmpty());
        assertEquals(Optional.of(new Lesson("Sunday0000")), withoutCost.getLesson());

        Person withoutLesson = addTo(new ModelManager(), BASE + " sub/A cost/1");
        assertEquals(Optional.of(new Cost("1")), withoutLesson.getCost());
        assertTrue(withoutLesson.getLesson().isEmpty());
    }

    @Test
    public void add_reorderedFieldsAndRepeatedSubjects_accepted() throws Exception {
        Person person = addTo(new ModelManager(),
                " cost/30 sub/Math n/John Davis c/Monday1800 a/Clementi"
                        + " e/johndavis@gmail.com p/91234567 sub/Math sub/A");
        assertEquals(Set.of(new Subject("Math"), new Subject("A")), person.getSubjects());
        assertEquals(Optional.of(new Cost("30")), person.getCost());
        assertEquals(Optional.of(new Lesson("Monday1800")), person.getLesson());
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
    }

    @Test
    public void add_duplicateNameWithDifferentDetails_rejected() throws Exception {
        Model model = new ModelManager();
        Person original = addTo(model, BASE + DETAILS);
        CommandException error = assertThrows(CommandException.class, () ->
                addTo(model, BASE + " sub/Physics cost/40 c/Tuesday1900"));
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
        assertEquals(original.getSubjects(), edited.getSubjects());
        assertEquals(original.getCost(), edited.getCost());
        assertEquals(original.getLesson(), edited.getLesson());
    }

    @Test
    public void tagAndUntag_preserveStudentDetails() throws Exception {
        Model model = new ModelManager();
        Person original = addTo(model, BASE + " t/friends" + DETAILS);

        new AddressBookParser().parseCommand("tag 1 exam-prep").execute(model);
        Person tagged = model.getFilteredPersonList().get(0);
        assertEquals(Set.of(new Tag("friends"), new Tag("exam-prep")), tagged.getTags());
        assertEquals(original.getSubjects(), tagged.getSubjects());
        assertEquals(original.getCost(), tagged.getCost());
        assertEquals(original.getLesson(), tagged.getLesson());

        new AddressBookParser().parseCommand("untag 1 friends").execute(model);
        Person untagged = model.getFilteredPersonList().get(0);
        assertEquals(Set.of(new Tag("exam-prep")), untagged.getTags());
        assertEquals(original.getSubjects(), untagged.getSubjects());
        assertEquals(original.getCost(), untagged.getCost());
        assertEquals(original.getLesson(), untagged.getLesson());

        new AddressBookParser().parseCommand("untag 1 -all").execute(model);
        Person withoutTags = model.getFilteredPersonList().get(0);
        assertTrue(withoutTags.getTags().isEmpty());
        assertEquals(original.getSubjects(), withoutTags.getSubjects());
        assertEquals(original.getCost(), withoutTags.getCost());
        assertEquals(original.getLesson(), withoutTags.getLesson());
    }

    @Test
    public void saveAndReload_retainsStudentDetailsAndOmittedCost() throws Exception {
        for (String details : List.of(DETAILS, " sub/A c/Sunday2359", "")) {
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
    public void loadLegacyJson_withoutStudentDetails_accepted() throws Exception {
        String json = "{\"name\":\"John Davis\",\"phone\":\"91234567\","
                + "\"email\":\"johndavis@gmail.com\",\"address\":\"Clementi\",\"tags\":[]}";
        Person restored = JsonUtil.fromJsonString(json, JsonAdaptedPerson.class).toModelType();
        assertEquals(addTo(new ModelManager(), BASE), restored);
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
}
