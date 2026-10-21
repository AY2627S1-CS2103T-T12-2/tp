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
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.person.Person;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_ENROLMENT =
            "A student has duplicate enrolments for the same class.";
    public static final String MESSAGE_CLASS_COST_CONFLICT = "Students in the same class have different costs.";

    private final List<JsonAdaptedPerson> persons = new ArrayList<>();
    private final List<JsonAdaptedTuitionClass> classes = new ArrayList<>();
    private final Integer nextClassSequence;

    /**
     * Constructs a {@code JsonSerializableAddressBook} with the given persons.
     */
    @JsonCreator
    public JsonSerializableAddressBook(@JsonProperty("persons") List<JsonAdaptedPerson> persons,
            @JsonProperty("classes") List<JsonAdaptedTuitionClass> classes,
            @JsonProperty("nextClassSequence") Integer nextClassSequence) {
        if (persons != null) {
            this.persons.addAll(persons);
        }
        if (classes != null) {
            this.classes.addAll(classes);
        }
        this.nextClassSequence = nextClassSequence;
    }

    /** Backwards-compatible constructor for files containing only people. */
    public JsonSerializableAddressBook(List<JsonAdaptedPerson> persons) {
        this(persons, null, null);
    }

    /**
     * Converts a given {@code ReadOnlyAddressBook} into this class for Jackson use.
     *
     * @param source future changes to this will not affect the created {@code JsonSerializableAddressBook}.
     */
    public JsonSerializableAddressBook(ReadOnlyAddressBook source) {
        persons.addAll(source.getPersonList().stream().map(JsonAdaptedPerson::new).collect(Collectors.toList()));
        classes.addAll(source.getTuitionClassList().stream()
                .map(JsonAdaptedTuitionClass::new).collect(Collectors.toList()));
        nextClassSequence = source.getNextClassSequence();
    }

    /**
     * Converts this address book into the model's {@code AddressBook} object.
     *
     * @throws IllegalValueException if there were any data constraints violated.
     */
    public AddressBook toModelType() throws IllegalValueException {
        AddressBook addressBook = new AddressBook();
        for (JsonAdaptedTuitionClass adaptedClass : classes) {
            TuitionClass tuitionClass = adaptedClass.toModelType();
            if (addressBook.hasTuitionClass(tuitionClass.getClassId())
                    || addressBook.hasTuitionClassWithDetails(tuitionClass)) {
                throw new IllegalValueException("Classes list contains duplicate class IDs or class details.");
            }
            addressBook.addTuitionClass(tuitionClass);
        }
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
            boolean hasDuplicateEnrolment = person.getLessons().stream().anyMatch(first ->
                    person.getLessons().stream().anyMatch(second -> first != second && first.isSameClass(second)));
            if (hasDuplicateEnrolment) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_ENROLMENT);
            }
            boolean hasCostConflict = addressBook.getPersonList().stream()
                    .flatMap(existingPerson -> existingPerson.getLessons().stream())
                    .anyMatch(existingLesson -> person.getLessons().stream().anyMatch(candidateLesson ->
                            existingLesson.isSameClass(candidateLesson)
                                    && !existingLesson.getCost().equals(candidateLesson.getCost())));
            if (hasCostConflict) {
                throw new IllegalValueException(MESSAGE_CLASS_COST_CONFLICT);
            }
            java.util.Set<Lesson> linkedLessons = new java.util.LinkedHashSet<>();
            for (Lesson lesson : person.getLessons()) {
                TuitionClass tuitionClass = null;
                if (lesson.getClassId() != null && addressBook.hasTuitionClass(lesson.getClassId())) {
                    tuitionClass = addressBook.getTuitionClass(lesson.getClassId());
                }
                if (tuitionClass == null) {
                    tuitionClass = addressBook.getTuitionClassList().stream()
                            .filter(item -> item.getSubject().value.equalsIgnoreCase(lesson.getSubject().value)
                                    && item.getSchedule().equals(lesson.getSchedule()))
                            .findFirst().orElse(null);
                }
                if (tuitionClass == null) {
                    seedu.address.model.lesson.ClassId id = lesson.getClassId() != null
                            ? lesson.getClassId() : addressBook.allocateClassId();
                    tuitionClass = new TuitionClass(id, lesson.getSubject(), lesson.getCost(), lesson.getSchedule());
                    addressBook.addTuitionClass(tuitionClass);
                }
                if (!tuitionClass.getCost().equals(lesson.getCost())) {
                    throw new IllegalValueException(MESSAGE_CLASS_COST_CONFLICT);
                }
                linkedLessons.add(new Enrollment(tuitionClass, lesson.getPaymentStatus()));
            }
            addressBook.addPerson(person.withLessons(linkedLessons));
        }
        int minimumNext = addressBook.getTuitionClassList().stream()
                .mapToInt(item -> item.getClassId().getSequence()).max().orElse(0) + 1;
        addressBook.setNextClassSequence(Math.max(minimumNext,
                nextClassSequence == null ? 1 : nextClassSequence));
        return addressBook;
    }

}
