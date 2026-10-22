package seedu.address.model.person;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.EnrollmentId;
import seedu.address.model.person.exceptions.DuplicatePersonException;
import seedu.address.model.person.exceptions.PersonNotFoundException;

/**
 * A list of persons that enforces uniqueness between its elements and does not allow nulls.
 * A person is considered unique by both the user-facing identity defined by
 * {@code Person#isSamePerson(Person)}, persisted {@code StudentId}, and persisted {@code EnrollmentId} values.
 * However, the removal of a person uses Person#equals(Object) so as to ensure that the person with exactly the same
 * fields will be removed.
 *
 * Supports a minimal set of list operations.
 *
 * @see Person#isSamePerson(Person)
 */
public class UniquePersonList implements Iterable<Person> {

    private final ObservableList<Person> internalList = FXCollections.observableArrayList();
    private final ObservableList<Person> internalUnmodifiableList =
            FXCollections.unmodifiableObservableList(internalList);

    /**
     * Returns true if the list contains an equivalent person as the given argument.
     */
    public boolean contains(Person toCheck) {
        requireNonNull(toCheck);
        return internalList.stream().anyMatch(toCheck::isSamePerson);
    }

    /**
     * Adds a person to the list.
     * The person must not already exist in the list.
     */
    public void add(Person toAdd) {
        requireNonNull(toAdd);
        if (contains(toAdd) || containsStudentId(toAdd.getStudentId()) || hasDuplicateEnrollmentId(toAdd)) {
            throw new DuplicatePersonException();
        }
        internalList.add(toAdd);
    }

    /**
     * Replaces the person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the list.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the list.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireAllNonNull(target, editedPerson);

        int index = internalList.indexOf(target);
        if (index == -1) {
            throw new PersonNotFoundException();
        }

        List<Person> updatedPersons = new java.util.ArrayList<>(internalList);
        updatedPersons.set(index, editedPerson);
        if (!personsAreUnique(updatedPersons)) {
            throw new DuplicatePersonException();
        }

        internalList.set(index, editedPerson);
    }

    /**
     * Removes the equivalent person from the list.
     * The person must exist in the list.
     */
    public void remove(Person toRemove) {
        requireNonNull(toRemove);
        if (!internalList.remove(toRemove)) {
            throw new PersonNotFoundException();
        }
    }

    public void setPersons(UniquePersonList replacement) {
        requireNonNull(replacement);
        setPersons(replacement.internalList);
    }

    /**
     * Replaces the contents of this list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        requireAllNonNull(persons);
        if (!personsAreUnique(persons)) {
            throw new DuplicatePersonException();
        }

        internalList.setAll(persons);
    }

    /**
     * Returns the backing list as an unmodifiable {@code ObservableList}.
     */
    public ObservableList<Person> asUnmodifiableObservableList() {
        return internalUnmodifiableList;
    }

    @Override
    public Iterator<Person> iterator() {
        return internalList.iterator();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof UniquePersonList otherUniquePersonList)) {
            return false;
        }

        return internalList.equals(otherUniquePersonList.internalList);
    }

    @Override
    public int hashCode() {
        return internalList.hashCode();
    }

    @Override
    public String toString() {
        return internalList.toString();
    }

    /**
     * Returns true if {@code persons} contains only unique persons.
     */
    private boolean personsAreUnique(List<Person> persons) {
        Set<StudentId> studentIds = new HashSet<>();
        Set<EnrollmentId> enrollmentIds = new HashSet<>();
        for (int i = 0; i < persons.size(); i++) {
            Person person = persons.get(i);
            if (!studentIds.add(person.getStudentId())) {
                return false;
            }
            for (Enrollment enrollment : person.getEnrollments()) {
                if (!enrollmentIds.add(enrollment.getEnrollmentId())) {
                    return false;
                }
            }
            for (int j = 0; j < i; j++) {
                if (person.isSamePerson(persons.get(j))) {
                    return false;
                }
            }
        }
        return true;
    }

    private boolean containsStudentId(StudentId studentId) {
        return internalList.stream().anyMatch(person -> person.getStudentId().equals(studentId));
    }

    private boolean hasDuplicateEnrollmentId(Person person) {
        return person.getEnrollments().stream()
                .map(Enrollment::getEnrollmentId)
                .anyMatch(enrollmentId -> internalList.stream()
                        .flatMap(existingPerson -> existingPerson.getEnrollments().stream())
                        .anyMatch(existingEnrollment -> existingEnrollment.getEnrollmentId().equals(enrollmentId)));
    }
}
