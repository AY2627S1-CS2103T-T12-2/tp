package seedu.address.logic;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.model.Model;
import seedu.address.model.ModelManager;
import seedu.address.model.UserPrefs;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;
import seedu.address.storage.JsonAddressBookStorage;
import seedu.address.storage.JsonUserPrefsStorage;
import seedu.address.storage.StorageManager;
import seedu.address.testutil.PersonBuilder;

/** Exercises subject search through the command, model, and real JSON storage layers. */
public class SubjectSearchLogicIntegrationTest {

    @TempDir
    public Path temporaryFolder;

    private Model model;
    private Logic logic;
    private StorageManager storage;
    private Person alice;
    private Person bob;

    @BeforeEach
    public void setUp() {
        model = new ModelManager();
        alice = new PersonBuilder().withName("Alice").withLessons(lesson("Math")).build();
        bob = new PersonBuilder().withName("Bob").withLessons(lesson("Physics")).build();
        model.addPerson(alice);
        model.addPerson(bob);
        storage = new StorageManager(new JsonAddressBookStorage(temporaryFolder.resolve("students.json")),
                new JsonUserPrefsStorage(temporaryFolder.resolve("preferences.json")));
        logic = new LogicManager(model, storage);
    }

    @Test
    public void execute_searchAndRestart_preservesAllStudentsAndEnrolments() throws Exception {
        logic.execute("list");
        String savedData = Files.readString(storage.getAddressBookFilePath());

        logic.execute("find sub/phys");
        assertEquals(List.of(bob), logic.getFilteredPersonList());
        assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        assertEquals(List.of(alice, bob), reloaded.getAddressBook().getPersonList());

        Logic restartedLogic = new LogicManager(reloaded, storage);
        restartedLogic.execute("find sub/MATH");
        assertEquals(List.of(alice), restartedLogic.getFilteredPersonList());
        restartedLogic.execute("list");
        assertEquals(List.of(alice, bob), restartedLogic.getFilteredPersonList());
    }

    @Test
    public void execute_invalidSubjectSearch_keepsExistingResultsAndSavedData() throws Exception {
        logic.execute("find sub/Physics");
        String savedData = Files.readString(storage.getAddressBookFilePath());

        for (String input : List.of("find sub/", "find sub/Math!", "find sub/Math sub/Physics",
                "find sub/Math t/priority", "find Alice sub/Math", "find c/Monday1800 sub/Math")) {
            assertThrows(ParseException.class, () -> logic.execute(input));
            assertEquals(List.of(bob), logic.getFilteredPersonList());
            assertEquals(List.of(alice, bob), model.getAddressBook().getPersonList());
            assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));
        }
    }

    @Test
    public void execute_subjectSearchInTimetable_requiresReturningToList() throws Exception {
        logic.execute("timetable");
        String savedData = Files.readString(storage.getAddressBookFilePath());

        CommandException error = assertThrows(CommandException.class, () -> logic.execute("find sub/Math"));
        assertEquals(LogicManager.MESSAGE_TIMETABLE_READ_ONLY, error.getMessage());
        assertEquals(List.of(alice, bob), logic.getFilteredPersonList());
        assertEquals(savedData, Files.readString(storage.getAddressBookFilePath()));

        logic.execute("list");
        logic.execute("find sub/Math");
        assertEquals(List.of(alice), logic.getFilteredPersonList());
    }

    @Test
    public void execute_deleteAfterSubjectSearch_usesDisplayedIndexAndPersistsDeletion() throws Exception {
        logic.execute("find sub/Physics");
        logic.execute("delete 1");

        assertEquals(List.of(alice), model.getAddressBook().getPersonList());
        assertEquals(List.of(alice), storage.readAddressBook().orElseThrow().getPersonList());
        logic.execute("list");
        assertEquals(List.of(alice), logic.getFilteredPersonList());
    }

    @Test
    public void execute_legacySubjectOnlyRecord_remainsSearchableAfterRestart() throws Exception {
        Person base = new PersonBuilder().withName("Carol").build();
        Person legacyStudent = new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getTags(), Set.of(new seedu.address.model.person.Subject("Computer Science")),
                Optional.empty(), Optional.empty());
        model.addPerson(legacyStudent);
        logic.execute("list");
        Model reloaded = new ModelManager(storage.readAddressBook().orElseThrow(), new UserPrefs());
        Logic restartedLogic = new LogicManager(reloaded, storage);

        restartedLogic.execute("find sub/SCI");
        assertEquals(List.of(legacyStudent), restartedLogic.getFilteredPersonList());
        assertEquals(3, reloaded.getAddressBook().getPersonList().size());
    }

    private Lesson lesson(String subject) {
        return new Lesson(new Subject(subject), new Cost("30"), new LessonTiming("Monday1800-1930"));
    }
}
