package seedu.address.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static seedu.address.logic.commands.CommandTestUtil.VALID_ADDRESS_BOB;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;
import static seedu.address.testutil.Assert.assertThrows;
import static seedu.address.testutil.TypicalPersons.ALICE;
import static seedu.address.testutil.TypicalPersons.BENSON;
import static seedu.address.testutil.TypicalPersons.CARL;
import static seedu.address.testutil.TypicalPersons.DANIEL;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;

import javafx.collections.ListChangeListener;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.OneOffLessonSchedule;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.NameContainsKeywordsPredicate;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.timetable.TimetableEntry;
import seedu.address.testutil.AddressBookBuilder;
import seedu.address.testutil.PersonBuilder;

public class ModelManagerTest {

    private ModelManager modelManager = new ModelManager();

    @Test
    public void constructor() {
        assertEquals(new UserPrefs(), modelManager.getUserPrefs());
        assertEquals(new GuiSettings(), modelManager.getGuiSettings());
        assertFalse(modelManager.isPrivacyModeEnabled());
        assertEquals(new AddressBook(), new AddressBook(modelManager.getAddressBook()));
    }

    @Test
    public void constructor_validUserPrefs_copiesUserPrefs() {
        UserPrefs userPrefs = new UserPrefs();
        userPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        userPrefs.setPrivacyModeEnabled(true);
        modelManager = new ModelManager(new AddressBook(), userPrefs);
        assertEquals(userPrefs, modelManager.getUserPrefs());

        // Modifying userPrefs should not modify modelManager's userPrefs
        UserPrefs oldUserPrefs = new UserPrefs(userPrefs);
        userPrefs.setGuiSettings(new GuiSettings(5, 6, 7, 8));
        userPrefs.setPrivacyModeEnabled(false);
        assertEquals(oldUserPrefs, modelManager.getUserPrefs());
    }

    @Test
    public void setGuiSettings_nullGuiSettings_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setGuiSettings(null));
    }

    @Test
    public void setGuiSettings_validGuiSettings_setsGuiSettings() {
        GuiSettings guiSettings = new GuiSettings(1, 2, 3, 4);
        modelManager.setGuiSettings(guiSettings);
        assertEquals(guiSettings, modelManager.getGuiSettings());
    }

    @Test
    public void setPrivacyModeEnabled_validValue_setsPrivacyMode() {
        modelManager.setPrivacyModeEnabled(true);
        assertTrue(modelManager.isPrivacyModeEnabled());

        modelManager.setPrivacyModeEnabled(false);
        assertFalse(modelManager.isPrivacyModeEnabled());
    }

    @Test
    public void hasPerson_nullPerson_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.hasPerson(null));
    }

    @Test
    public void hasPerson_personNotInAddressBook_returnsFalse() {
        assertFalse(modelManager.hasPerson(ALICE));
    }

    @Test
    public void hasPerson_personInAddressBook_returnsTrue() {
        modelManager.addPerson(ALICE);
        assertTrue(modelManager.hasPerson(ALICE));
    }

    @Test
    public void setPersons_validReplacements_updatesPersonsInOneChange() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        modelManager = new ModelManager(addressBook, new UserPrefs());
        Person editedAlice = new PersonBuilder(ALICE).withAddress(VALID_ADDRESS_BOB).build();
        Person editedBenson = new PersonBuilder(BENSON).withAddress("Edited address").build();
        AtomicInteger changeCount = new AtomicInteger();
        modelManager.getAddressBook().getPersonList().addListener((ListChangeListener<Person>)
                change -> changeCount.incrementAndGet());

        modelManager.setPersons(Map.of(ALICE, editedAlice, BENSON, editedBenson));

        assertEquals(List.of(editedAlice, editedBenson), modelManager.getAddressBook().getPersonList());
        assertEquals(1, changeCount.get());
    }

    @Test
    public void setPersons_emptyReplacements_makesNoChange() {
        modelManager.addPerson(ALICE);
        AtomicInteger changeCount = new AtomicInteger();
        modelManager.getAddressBook().getPersonList().addListener((ListChangeListener<Person>)
                change -> changeCount.incrementAndGet());

        modelManager.setPersons(Map.of());

        assertEquals(List.of(ALICE), modelManager.getAddressBook().getPersonList());
        assertEquals(0, changeCount.get());
    }

    @Test
    public void setPersons_missingTarget_throwsWithoutChanges() {
        modelManager.addPerson(ALICE);
        Person missingPerson = new PersonBuilder().withName("Missing Person").build();
        AddressBook expectedAddressBook = new AddressBook(modelManager.getAddressBook());

        assertThrows(PersonNotFoundException.class, ()
                -> modelManager.setPersons(Map.of(missingPerson, BENSON)));
        assertEquals(expectedAddressBook, modelManager.getAddressBook());
    }

    @Test
    public void setPersons_duplicateResult_throwsWithoutChanges() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        modelManager = new ModelManager(addressBook, new UserPrefs());
        AddressBook expectedAddressBook = new AddressBook(modelManager.getAddressBook());

        assertThrows(DuplicatePersonException.class, ()
                -> modelManager.setPersons(Map.of(ALICE, BENSON)));
        assertEquals(expectedAddressBook, modelManager.getAddressBook());
    }

    @Test
    public void setPersons_nullInput_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.setPersons(null));

        Map<Person, Person> nullTarget = new HashMap<>();
        nullTarget.put(null, ALICE);
        assertThrows(NullPointerException.class, () -> modelManager.setPersons(nullTarget));

        Map<Person, Person> nullReplacement = new HashMap<>();
        nullReplacement.put(ALICE, null);
        assertThrows(NullPointerException.class, () -> modelManager.setPersons(nullReplacement));
    }

    @Test
    public void getFilteredPersonList_modifyList_throwsUnsupportedOperationException() {
        assertThrows(UnsupportedOperationException.class, () -> modelManager.getFilteredPersonList().remove(0));
    }

    @Test
    public void constructor_pinnedAndUnpinnedPersons_placesPinnedFirstAndPreservesGroupOrder() {
        Person pinnedBenson = new PersonBuilder(BENSON).withPinned(true).build();
        Person pinnedDaniel = new PersonBuilder(DANIEL).withPinned(true).build();
        AddressBook addressBook = new AddressBookBuilder()
                .withPerson(ALICE)
                .withPerson(pinnedBenson)
                .withPerson(CARL)
                .withPerson(pinnedDaniel)
                .build();

        modelManager = new ModelManager(addressBook, new UserPrefs());

        assertEquals(List.of(pinnedBenson, pinnedDaniel, ALICE, CARL),
                modelManager.getFilteredPersonList());
    }

    @Test
    public void setPerson_unpinnedPersonBecomesPinned_reordersListImmediately() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        modelManager = new ModelManager(addressBook, new UserPrefs());
        Person pinnedBenson = BENSON.withPinned(true);

        modelManager.setPerson(BENSON, pinnedBenson);

        assertEquals(List.of(pinnedBenson, ALICE), modelManager.getFilteredPersonList());
    }

    @Test
    public void updateFilteredPersonList_customComparator_ordersWithinPinGroups() {
        Person pinnedBenson = BENSON.withPinned(true);
        Person pinnedDaniel = DANIEL.withPinned(true);
        AddressBook addressBook = new AddressBookBuilder()
                .withPerson(ALICE)
                .withPerson(pinnedBenson)
                .withPerson(CARL)
                .withPerson(pinnedDaniel)
                .build();
        modelManager = new ModelManager(addressBook, new UserPrefs());
        Comparator<Person> reverseNameOrder = Comparator.comparing(
                person -> person.getName().fullName, Comparator.reverseOrder());

        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS, reverseNameOrder);

        assertEquals(List.of(pinnedDaniel, pinnedBenson, CARL, ALICE),
                modelManager.getFilteredPersonList());
    }

    @Test
    public void getTimetable_filteredPersonList_usesAllAddressBookPersons() {
        Lesson lesson = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Thursday1800-1930"));
        Person aliceWithLesson = new PersonBuilder(ALICE).withLessons(lesson).build();
        Person bensonWithLesson = new PersonBuilder(BENSON).withLessons(lesson).build();
        AddressBook addressBook = new AddressBookBuilder()
                .withPerson(aliceWithLesson).withPerson(bensonWithLesson).build();
        modelManager = new ModelManager(addressBook, new UserPrefs());
        modelManager.updateFilteredPersonList(
                new NameContainsKeywordsPredicate(List.of(ALICE.getName().fullName.split(" ")[0])));

        TimetableEntry entry = modelManager.getTimetable(LocalDate.of(2026, 10, 8))
                .get(4).getEntries().get(0);

        assertEquals(1, modelManager.getFilteredPersonList().size());
        assertEquals(List.of(aliceWithLesson, bensonWithLesson), entry.getStudents());
    }

    @Test
    public void findOverlappingClasses_multiplePersons_returnsDistinctSortedClasses() {
        Lesson candidate = new Lesson(new Subject("Chemistry"), new Cost("40"),
                new LessonTiming("Monday1800-1900"));
        Lesson earlierConflict = new Lesson(new Subject("Physics"), new Cost("35"),
                new LessonTiming("Monday1700-1830"));
        Lesson laterConflict = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Lesson sameClass = new Lesson(new Subject("chemistry"), new Cost("40"),
                new LessonTiming("Monday1800-1900"));
        Lesson backToBack = new Lesson(new Subject("English"), new Cost("25"),
                new LessonTiming("Monday1900-2000"));
        Lesson differentDay = new Lesson(new Subject("Biology"), new Cost("30"),
                new LessonTiming("Tuesday1800-1900"));
        Person aliceWithConflicts = new PersonBuilder(ALICE)
                .withLessons(laterConflict, earlierConflict, sameClass).build();
        Person bensonWithDuplicate = new PersonBuilder(BENSON)
                .withLessons(laterConflict.withPaymentStatus(
                        seedu.address.model.lesson.PaymentStatus.PAID)).build();
        Person otherLessons = new PersonBuilder().withName("Other Student").withPhone("81234567")
                .withEmail("other@example.com").withLessons(backToBack, differentDay).build();
        AddressBook addressBook = new AddressBookBuilder()
                .withPerson(aliceWithConflicts)
                .withPerson(bensonWithDuplicate)
                .withPerson(otherLessons)
                .build();
        modelManager = new ModelManager(addressBook, new UserPrefs());
        modelManager.updateFilteredPersonList(
                new NameContainsKeywordsPredicate(List.of(otherLessons.getName().fullName.split(" ")[0])));

        assertEquals(List.of(earlierConflict, laterConflict), modelManager.findOverlappingClasses(candidate));
    }

    @Test
    public void findOverlappingClasses_weeklyAndOneOff_returnsDateAwareConflicts() {
        Lesson weekly = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Tuesday1800-1930"));
        Lesson differentDate = new Lesson(new Subject("English"), new Cost("25"),
                new OneOffLessonSchedule("2026-10-21T1800-1930"));
        Person student = new PersonBuilder(ALICE).withLessons(weekly, differentDate).build();
        modelManager = new ModelManager(new AddressBookBuilder().withPerson(student).build(), new UserPrefs());

        Lesson overlappingTuesday = new Lesson(new Subject("Physics"), new Cost("35"),
                new OneOffLessonSchedule("2026-10-20T1900-2000"));
        Lesson nonOverlappingTuesday = new Lesson(new Subject("Physics"), new Cost("35"),
                new OneOffLessonSchedule("2026-10-20T1930-2000"));

        assertEquals(List.of(weekly), modelManager.findOverlappingClasses(overlappingTuesday));
        assertTrue(modelManager.findOverlappingClasses(nonOverlappingTuesday).isEmpty());
    }

    @Test
    public void findOverlappingClasses_nullCandidate_throwsNullPointerException() {
        assertThrows(NullPointerException.class, () -> modelManager.findOverlappingClasses(null));
    }

    @Test
    public void equals() {
        AddressBook addressBook = new AddressBookBuilder().withPerson(ALICE).withPerson(BENSON).build();
        AddressBook differentAddressBook = new AddressBook();
        UserPrefs userPrefs = new UserPrefs();

        // same values -> returns true
        modelManager = new ModelManager(addressBook, userPrefs);
        ModelManager modelManagerCopy = new ModelManager(addressBook, userPrefs);
        assertTrue(modelManager.equals(modelManagerCopy));

        // same object -> returns true
        assertTrue(modelManager.equals(modelManager));

        // null -> returns false
        assertFalse(modelManager.equals(null));

        // different types -> returns false
        assertFalse(modelManager.equals(5));

        // different addressBook -> returns false
        assertFalse(modelManager.equals(new ModelManager(differentAddressBook, userPrefs)));

        // different filteredList -> returns false
        String[] keywords = ALICE.getName().fullName.split("\\s+");
        modelManager.updateFilteredPersonList(new NameContainsKeywordsPredicate(List.of(keywords)));
        assertFalse(modelManager.equals(new ModelManager(addressBook, userPrefs)));

        // resets modelManager to initial state for upcoming tests
        modelManager.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);

        // different userPrefs -> returns false
        UserPrefs differentUserPrefs = new UserPrefs();
        differentUserPrefs.setGuiSettings(new GuiSettings(1, 2, 3, 4));
        assertFalse(modelManager.equals(new ModelManager(addressBook, differentUserPrefs)));
    }
}
