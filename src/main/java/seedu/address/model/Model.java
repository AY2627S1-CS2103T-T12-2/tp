package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.LessonScheduleComparator;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.person.Person;
import seedu.address.model.timetable.TimetableDay;

/**
 * The API of the Model component.
 */
public interface Model {
    /** {@code Predicate} that always evaluates to true */
    Predicate<Person> PREDICATE_SHOW_ALL_PERSONS = unused -> true;

    /**
     * Returns the user prefs.
     */
    ReadOnlyUserPrefs getUserPrefs();

    /**
     * Returns the user prefs' GUI settings.
     */
    GuiSettings getGuiSettings();

    /**
     * Sets the user prefs' GUI settings.
     */
    void setGuiSettings(GuiSettings guiSettings);

    /** Returns whether privacy mode is enabled. */
    boolean isPrivacyModeEnabled();

    /** Enables or disables privacy mode. */
    void setPrivacyModeEnabled(boolean privacyModeEnabled);

    /**
     * Replaces address book data with the data in {@code addressBook}.
     */
    void setAddressBook(ReadOnlyAddressBook addressBook);

    /** Returns the AddressBook */
    ReadOnlyAddressBook getAddressBook();

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    boolean hasPerson(Person person);

    /**
     * Deletes the given person.
     * The person must exist in the address book.
     */
    void deletePerson(Person target);

    /**
     * Adds the given person.
     * {@code person} must not already exist in the address book.
     */
    void addPerson(Person person);

    /**
     * Replaces the given person {@code target} with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    void setPerson(Person target, Person editedPerson);

    /**
     * Atomically replaces every target person with its corresponding edited person.
     * Every target must exist, and the resulting persons must have unique identities.
     */
    void setPersons(Map<Person, Person> replacements);

    /** Allocates and returns the next stable class ID. */
    default ClassId allocateClassId() {
        throw new UnsupportedOperationException();
    }

    /** Returns whether the supplied stable class ID exists. */
    default boolean hasTuitionClass(ClassId classId) {
        return getAddressBook().getTuitionClassList().stream()
                .anyMatch(item -> item.getClassId().equals(classId));
    }

    default boolean hasTuitionClassWithDetails(TuitionClass tuitionClass) {
        return getAddressBook().getTuitionClassList().stream().anyMatch(item -> item.hasSameDetails(tuitionClass));
    }

    default TuitionClass getTuitionClass(ClassId classId) {
        return getAddressBook().getTuitionClassList().stream()
                .filter(item -> item.getClassId().equals(classId)).findFirst()
                .orElseThrow(java.util.NoSuchElementException::new);
    }

    /**
     * Finds a centrally stored class by its case-insensitive subject and complete lesson schedule.
     */
    default Optional<TuitionClass> findTuitionClass(Subject subject, LessonSchedule schedule) {
        requireNonNull(subject);
        requireNonNull(schedule);
        return getAddressBook().getTuitionClassList().stream()
                .filter(item -> item.getSubject().equals(subject) && item.getSchedule().equals(schedule))
                .findFirst();
    }

    default void addTuitionClass(TuitionClass tuitionClass) {
        throw new UnsupportedOperationException();
    }

    default void setTuitionClass(TuitionClass target, TuitionClass edited) {
        throw new UnsupportedOperationException();
    }

    default void deleteTuitionClass(TuitionClass tuitionClass) {
        throw new UnsupportedOperationException();
    }

    default ObservableList<TuitionClass> getTuitionClassList() {
        return getAddressBook().getTuitionClassList();
    }

    /** Returns an unmodifiable view of the filtered person list */
    ObservableList<Person> getFilteredPersonList();

    /**
     * Updates the filter of the filtered person list to filter by the given {@code predicate}.
     * @throws NullPointerException if {@code predicate} is null.
     */
    void updateFilteredPersonList(Predicate<Person> predicate);

    /**
     * Updates the filtered person list and sorts matching persons with the given comparator.
     * @throws NullPointerException if {@code predicate} or {@code comparator} is null.
     */
    default void updateFilteredPersonList(Predicate<Person> predicate, Comparator<Person> comparator) {
        updateFilteredPersonList(predicate);
    }

    /** Returns the timetable derived from all persons for the specified date. */
    List<TimetableDay> getTimetable(LocalDate today);

    /** Returns whether an existing class has the same class with a different cost. */
    default boolean hasClassCostConflict(Lesson candidate) {
        requireNonNull(candidate);
        return getTuitionClassList().stream()
                .anyMatch(existing ->
                        existing.getSubject().value.equalsIgnoreCase(candidate.getSubject().value)
                                && existing.getSchedule().equals(candidate.getSchedule())
                                && !existing.getCost().equals(candidate.getCost()));
    }

    /**
     * Returns the distinct existing classes that overlap {@code candidate}, ordered
     * by schedule, then subject. Copies of the same group class are returned once.
     */
    default List<Lesson> findOverlappingClasses(Lesson candidate) {
        requireNonNull(candidate);
        Comparator<Lesson> classOrder = Comparator
                .comparing(Lesson::getSchedule, LessonScheduleComparator.INSTANCE)
                .thenComparing(lesson -> lesson.getSubject().toString(), String.CASE_INSENSITIVE_ORDER);

        return getTuitionClassList().stream()
                .map(existing -> new Lesson(
                        existing.getClassId(),
                        existing.getSubject(),
                        existing.getCost(),
                        existing.getSchedule(),
                        PaymentStatus.UNPAID))
                .filter(existing -> !existing.isSameClass(candidate))
                .filter(existing -> existing.getSchedule().overlapsWith(candidate.getSchedule()))
                .sorted(classOrder)
                .toList();
    }

}
