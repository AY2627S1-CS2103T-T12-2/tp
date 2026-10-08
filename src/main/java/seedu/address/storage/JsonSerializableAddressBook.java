package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.person.Person;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_CLASS_COST_CONFLICT = "Students in the same class have different costs.";

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons) {
        if (persons != null) {
            this.persons.addAll(persons);
        }
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            boolean hasInternalCostConflict = person.getLessons().stream().anyMatch(first ->
                    person.getLessons().stream().anyMatch(second -> first != second && first.isSameClass(second)
                            && !first.getCost().equals(second.getCost())));
            if (hasInternalCostConflict) {
                throw new IllegalValueException(MESSAGE_CLASS_COST_CONFLICT);
            }
            boolean hasCostConflict = addressBook.getPersonList().stream()
                    .flatMap(existingPerson -> existingPerson.getLessons().stream())
                    .anyMatch(existingLesson -> person.getLessons().stream().anyMatch(candidateLesson ->
                            existingLesson.isSameClass(candidateLesson)
                                    && !existingLesson.getCost().equals(candidateLesson.getCost())));
            if (hasCostConflict) {
                throw new IllegalValueException(MESSAGE_CLASS_COST_CONFLICT);
            }
            addressBook.addPerson(person);
        }
        return addressBook;
    }

}
