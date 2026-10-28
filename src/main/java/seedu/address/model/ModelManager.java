package seedu.address.model;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.person.Person;
import seedu.address.model.person.exceptions.PersonNotFoundException;
import seedu.address.model.timetable.TimetableBuilder;
import seedu.address.model.timetable.TimetableDay;

/**
 * Represents the in-memory model of the address book data.
 */
public class ModelManager implements Model {
    private static final Logger logger = LogsCenter.getLogger(ModelManager.class);
    private static final Comparator<Person> PINNED_FIRST_COMPARATOR =
            Comparator.comparing(Person::isPinned).reversed();

    private final AddressBook addressBook;
    private final UserPrefs userPrefs;
    private final FilteredList<Person> filteredPersons;
    private final SortedList<Person> sortedFilteredPersons;

    /**
     * Initializes a ModelManager with the given addressBook and userPrefs.
     */
    public ModelManager(ReadOnlyAddressBook addressBook, ReadOnlyUserPrefs userPrefs) {
        requireAllNonNull(addressBook, userPrefs);

        logger.fine("Initializing model with " + addressBook.getPersonList().size() + " student(s) and "
                + addressBook.getTuitionClassList().size() + " class(es).");

        this.addressBook = new AddressBook(addressBook);
        this.userPrefs = new UserPrefs(userPrefs);
        normalizeExistingLessons();
        filteredPersons = new FilteredList<>(this.addressBook.getPersonList());
        sortedFilteredPersons = new SortedList<>(filteredPersons, getDefaultPersonComparator());
    }

    public ModelManager() {
        this(new AddressBook(), new UserPrefs());
    }

    //=========== UserPrefs ==================================================================================

    @Override
    public ReadOnlyUserPrefs getUserPrefs() {
        return userPrefs;
    }

    @Override
    public GuiSettings getGuiSettings() {
        return userPrefs.getGuiSettings();
    }

    @Override
    public void setGuiSettings(GuiSettings guiSettings) {
        requireNonNull(guiSettings);
        userPrefs.setGuiSettings(guiSettings);
    }

    @Override
    public boolean isPrivacyModeEnabled() {
        return userPrefs.isPrivacyModeEnabled();
    }

    @Override
    public void setPrivacyModeEnabled(boolean privacyModeEnabled) {
        userPrefs.setPrivacyModeEnabled(privacyModeEnabled);
    }

    //=========== AddressBook ================================================================================

    @Override
    public void setAddressBook(ReadOnlyAddressBook addressBook) {
        this.addressBook.resetData(addressBook);
    }

    @Override
    public ReadOnlyAddressBook getAddressBook() {
        return addressBook;
    }

    @Override
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return addressBook.hasPerson(person);
    }

    @Override
    public void deletePerson(Person target) {
        addressBook.removePerson(target);
    }

    @Override
    public void addPerson(Person person) {
        addressBook.addPerson(linkLessonsToClasses(person));
        updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
    }

    @Override
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);
        addressBook.setPerson(target, linkLessonsToClasses(editedPerson));
    }

    @Override
    public void setPersons(Map<Person, Person> replacements) {
        requireNonNull(replacements);
        requireAllNonNull(replacements.keySet());
        requireAllNonNull(replacements.values());
        if (replacements.isEmpty()) {
            return;
        }

        List<Person> updatedPersons = new ArrayList<>(addressBook.getPersonList());
        for (Map.Entry<Person, Person> replacement : replacements.entrySet()) {
            int targetIndex = addressBook.getPersonList().indexOf(replacement.getKey());
            if (targetIndex == -1) {
                throw new PersonNotFoundException();
            }
            updatedPersons.set(targetIndex, linkLessonsToClasses(replacement.getValue()));
        }
        addressBook.setPersons(updatedPersons);
    }

    private Person linkLessonsToClasses(Person person) {
        requireNonNull(person);
        java.util.Set<Enrollment> linkedEnrollments = new java.util.LinkedHashSet<>();
        for (Enrollment enrollment : person.getEnrollments()) {
            if (!addressBook.hasTuitionClass(enrollment.getClassId())) {
                throw new IllegalArgumentException("Enrollment refers to a class that does not exist: "
                        + enrollment.getClassId());
            }
            linkedEnrollments.add(enrollment.resolveWith(addressBook.getTuitionClass(enrollment.getClassId())));
        }
        for (Lesson lesson : person.getLegacyLessons()) {
            TuitionClass tuitionClass;
            if (lesson.getClassId() != null) {
                if (addressBook.hasTuitionClass(lesson.getClassId())) {
                    tuitionClass = addressBook.getTuitionClass(lesson.getClassId());
                } else {
                    tuitionClass = new TuitionClass(lesson.getClassId(), lesson.getSubject(),
                            lesson.getCost(), lesson.getSchedule(), lesson.getDetails());
                    addressBook.addTuitionClass(tuitionClass);
                }
            } else {
                tuitionClass = addressBook.getTuitionClassList().stream()
                        .filter(item -> item.getSubject().value.equalsIgnoreCase(lesson.getSubject().value)
                                && item.getSchedule().equals(lesson.getSchedule()))
                        .findFirst().orElseGet(() -> {
                            TuitionClass created = new TuitionClass(addressBook.allocateClassId(),
                                    lesson.getSubject(), lesson.getCost(), lesson.getSchedule(), lesson.getDetails());
                            addressBook.addTuitionClass(created);
                            return created;
                        });
            }
            if (!lesson.getDetails().equals(ClassDetails.EMPTY)
                    && !tuitionClass.getDetails().equals(lesson.getDetails())) {
                TuitionClass selected = tuitionClass;
                boolean hasStudents = addressBook.getPersonList().stream()
                        .flatMap(student -> student.getEnrollments().stream())
                        .anyMatch(item -> selected.getClassId().equals(item.getClassId()));
                if (tuitionClass.getDetails().equals(ClassDetails.EMPTY) && !hasStudents) {
                    TuitionClass updated = tuitionClass.withDetails(lesson.getDetails());
                    addressBook.setTuitionClass(tuitionClass, updated);
                    tuitionClass = updated;
                } else {
                    throw new IllegalArgumentException("Students in the same class have different details.");
                }
            }
            linkedEnrollments.add(new Enrollment(person.getStudentId(), tuitionClass,
                    lesson.getCost(), lesson.getPaymentStatus()));
        }
        return person.withEnrollments(linkedEnrollments);
    }

    private void normalizeExistingLessons() {
        for (Person person : List.copyOf(addressBook.getPersonList())) {
            Person linked = linkLessonsToClasses(person);
            boolean needsNormalization = !person.getLegacyLessons().isEmpty()
                    || person.getEnrollments().stream().anyMatch(enrollment -> !enrollment.isResolved());
            if (needsNormalization) {
                addressBook.setPerson(person, linked);
            }
        }
    }

    @Override
    public ClassId allocateClassId() {
        return addressBook.allocateClassId();
    }

    @Override
    public boolean hasTuitionClass(ClassId classId) {
        return addressBook.hasTuitionClass(classId);
    }

    @Override
    public boolean hasTuitionClassWithDetails(TuitionClass tuitionClass) {
        return addressBook.hasTuitionClassWithDetails(tuitionClass);
    }

    @Override
    public TuitionClass getTuitionClass(ClassId classId) {
        return addressBook.getTuitionClass(classId);
    }

    @Override
    public void addTuitionClass(TuitionClass tuitionClass) {
        addressBook.addTuitionClass(tuitionClass);
    }

    @Override
    public void setTuitionClass(TuitionClass target, TuitionClass edited) {
        addressBook.setTuitionClass(target, edited);
        for (Person person : List.copyOf(addressBook.getPersonList())) {
            java.util.Set<Enrollment> updated = person.getEnrollments().stream()
                    .map(enrollment -> target.getClassId().equals(enrollment.getClassId())
                            ? enrollment.resolveWith(edited) : enrollment)
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
            if (updated.stream().anyMatch(enrollment -> target.getClassId().equals(enrollment.getClassId()))) {
                addressBook.setPerson(person, person.withEnrollments(updated));
            }
        }
    }

    @Override
    public void deleteTuitionClass(TuitionClass tuitionClass) {
        for (Person person : List.copyOf(addressBook.getPersonList())) {
            java.util.Set<Enrollment> updated = person.getEnrollments().stream()
                    .filter(enrollment -> !tuitionClass.getClassId().equals(enrollment.getClassId()))
                    .collect(java.util.stream.Collectors.toCollection(java.util.LinkedHashSet::new));
            if (updated.size() != person.getEnrollments().size()) {
                addressBook.setPerson(person, person.withEnrollments(updated));
            }
        }
        addressBook.removeTuitionClass(tuitionClass);
    }

    @Override
    public ObservableList<TuitionClass> getTuitionClassList() {
        return addressBook.getTuitionClassList();
    }

    //=========== Filtered Person List Accessors =============================================================

    /**
     * Returns an unmodifiable view of the list of {@code Person} backed by the internal list of
     * {@code addressBook}
     */
    @Override
    public ObservableList<Person> getFilteredPersonList() {
        return sortedFilteredPersons;
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate) {
        requireNonNull(predicate);
        filteredPersons.setPredicate(predicate);
        sortedFilteredPersons.setComparator(getDefaultPersonComparator());
    }

    @Override
    public void updateFilteredPersonList(Predicate<Person> predicate, Comparator<Person> comparator) {
        requireAllNonNull(predicate, comparator);
        filteredPersons.setPredicate(predicate);
        sortedFilteredPersons.setComparator(PINNED_FIRST_COMPARATOR.thenComparing(comparator));
    }

    private Comparator<Person> getDefaultPersonComparator() {
        List<Person> originalOrder = addressBook.getPersonList();
        return PINNED_FIRST_COMPARATOR.thenComparingInt(originalOrder::indexOf);
    }

    @Override
    public List<TimetableDay> getTimetable(LocalDate anchorDate) {
        requireNonNull(anchorDate);
        return TimetableBuilder.build(addressBook.getTuitionClassList(), addressBook.getPersonList(), anchorDate);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof ModelManager otherModelManager)) {
            return false;
        }

        return addressBook.equals(otherModelManager.addressBook)
                && userPrefs.equals(otherModelManager.userPrefs)
                && sortedFilteredPersons.equals(otherModelManager.sortedFilteredPersons);
    }

}
