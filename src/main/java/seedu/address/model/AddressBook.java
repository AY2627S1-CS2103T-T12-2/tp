package seedu.address.model;

import static java.util.Objects.requireNonNull;

import java.util.List;

import javafx.collections.ObservableList;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.lesson.UniqueTuitionClassList;
import seedu.address.model.person.Person;
import seedu.address.model.person.UniquePersonList;

/**
 * Wraps all data at the address-book level.
 * Duplicates are not allowed (by .isSamePerson comparison).
 */
public class AddressBook implements ReadOnlyAddressBook {

    private final UniquePersonList persons = new UniquePersonList();
    private final UniqueTuitionClassList tuitionClasses = new UniqueTuitionClassList();
    private int nextClassSequence = 1;

    public AddressBook() {}

    /**
     * Creates an AddressBook using the Persons in the {@code toBeCopied}
     */
    public AddressBook(ReadOnlyAddressBook toBeCopied) {
        this();
        resetData(toBeCopied);
    }

    //// list overwrite operations

    /**
     * Replaces the contents of the person list with {@code persons}.
     * {@code persons} must not contain duplicate persons.
     */
    public void setPersons(List<Person> persons) {
        this.persons.setPersons(persons);
    }

    public void setTuitionClasses(List<TuitionClass> classes) {
        tuitionClasses.setClasses(classes);
    }

    public void setNextClassSequence(int nextClassSequence) {
        if (nextClassSequence < 1) {
            throw new IllegalArgumentException("Next class sequence must be positive.");
        }
        this.nextClassSequence = nextClassSequence;
    }

    /**
     * Resets the existing data of this {@code AddressBook} with {@code newData}.
     */
    public void resetData(ReadOnlyAddressBook newData) {
        requireNonNull(newData);

        setPersons(newData.getPersonList());
        setTuitionClasses(newData.getTuitionClassList());
        setNextClassSequence(newData.getNextClassSequence());
    }

    //// person-level operations

    /**
     * Returns true if a person with the same identity as {@code person} exists in the address book.
     */
    public boolean hasPerson(Person person) {
        requireNonNull(person);
        return persons.contains(person);
    }

    /**
     * Adds a person to the address book.
     * The person must not already exist in the address book.
     */
    public void addPerson(Person p) {
        persons.add(p);
    }

    /**
     * Replaces the given person {@code target} in the list with {@code editedPerson}.
     * {@code target} must exist in the address book.
     * The person identity of {@code editedPerson} must not be the same as another existing person in the address book.
     */
    public void setPerson(Person target, Person editedPerson) {
        requireNonNull(editedPerson);

        persons.setPerson(target, editedPerson);
    }

    /**
     * Removes {@code key} from this {@code AddressBook}.
     * {@code key} must exist in the address book.
     */
    public void removePerson(Person key) {
        persons.remove(key);
    }

    //// class-level operations

    /** Allocates the next stable class ID and advances the persisted sequence. */
    public ClassId allocateClassId() {
        ClassId allocated = ClassId.fromSequence(nextClassSequence);
        nextClassSequence++;
        return allocated;
    }

    public boolean hasTuitionClass(ClassId classId) {
        return tuitionClasses.containsId(classId);
    }

    public boolean hasTuitionClassWithDetails(TuitionClass tuitionClass) {
        return tuitionClasses.containsDetails(tuitionClass);
    }

    public TuitionClass getTuitionClass(ClassId classId) {
        return tuitionClasses.get(classId);
    }

    /** Adds an independently stored tuition class. */
    public void addTuitionClass(TuitionClass tuitionClass) {
        tuitionClasses.add(tuitionClass);
        nextClassSequence = Math.max(nextClassSequence, tuitionClass.getClassId().getSequence() + 1);
    }

    public void setTuitionClass(TuitionClass target, TuitionClass edited) {
        tuitionClasses.set(target, edited);
    }

    public void removeTuitionClass(TuitionClass tuitionClass) {
        tuitionClasses.remove(tuitionClass);
    }

    //// util methods

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this).add("persons", persons);
        return builder.toString();
    }

    @Override
    public ObservableList<Person> getPersonList() {
        return persons.asUnmodifiableObservableList();
    }

    @Override
    public ObservableList<TuitionClass> getTuitionClassList() {
        return tuitionClasses.asUnmodifiableObservableList();
    }

    @Override
    public int getNextClassSequence() {
        return nextClassSequence;
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof AddressBook otherAddressBook)) {
            return false;
        }

        boolean classesCompatible = tuitionClasses.asUnmodifiableObservableList().isEmpty()
                || otherAddressBook.tuitionClasses.asUnmodifiableObservableList().isEmpty()
                || tuitionClasses.equals(otherAddressBook.tuitionClasses)
                && nextClassSequence == otherAddressBook.nextClassSequence;
        return persons.equals(otherAddressBook.persons) && classesCompatible;
    }

    @Override
    public int hashCode() {
        return persons.hashCode();
    }
}
