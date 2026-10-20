package seedu.address.model;

import javafx.collections.ObservableList;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.person.Person;

/**
 * Unmodifiable view of an address book
 */
public interface ReadOnlyAddressBook {

    /**
     * Returns an unmodifiable view of the persons list.
     * This list will not contain any duplicate persons.
     */
    ObservableList<Person> getPersonList();

    /** Returns an unmodifiable view of all independently stored tuition classes. */
    default ObservableList<TuitionClass> getTuitionClassList() {
        return javafx.collections.FXCollections.emptyObservableList();
    }

    /** Returns the next sequence number used when allocating a class ID. */
    default int getNextClassSequence() {
        return 1;
    }

}
