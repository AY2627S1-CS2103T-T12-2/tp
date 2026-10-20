package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import javafx.collections.ObservableList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.model.lesson.Lesson;
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

    /** Returns whether an existing student has the same class with a different cost. */
    default boolean hasClassCostConflict(Lesson candidate) {
        return getAddressBook().getPersonList().stream()
                .flatMap(person -> person.getLessons().stream())
                .anyMatch(existing -> existing.isSameClass(candidate)
                        && !existing.getCost().equals(candidate.getCost()));
    }

    /**
     * Returns the distinct existing classes that overlap {@code candidate}, ordered by day,
     * start time, end time, then subject. Copies of the same group class are returned once.
     */
    default List<Lesson> findOverlappingClasses(Lesson candidate) {
        requireNonNull(candidate);
        Comparator<Lesson> classOrder = Comparator
                .comparingInt((Lesson lesson) -> lesson.getTiming().getDay().getValue())
                .thenComparing(lesson -> lesson.getTiming().getStartTime())
                .thenComparing(lesson -> lesson.getTiming().getEndTime())
                .thenComparing(lesson -> lesson.getSubject().toString(), String.CASE_INSENSITIVE_ORDER);

        List<Lesson> overlappingLessons = getAddressBook().getPersonList().stream()
                .flatMap(person -> person.getLessons().stream())
                .filter(existing -> !existing.isSameClass(candidate))
                .filter(existing -> existing.getTiming().overlapsWith(candidate.getTiming()))
                .sorted(classOrder)
                .toList();
        List<Lesson> distinctClasses = new ArrayList<>();
        for (Lesson lesson : overlappingLessons) {
            if (distinctClasses.stream().noneMatch(existing -> existing.isSameClass(lesson))) {
                distinctClasses.add(lesson);
            }
        }
        return List.copyOf(distinctClasses);
    }

}
