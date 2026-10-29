package seedu.address.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonRootName;

import seedu.address.commons.core.LogsCenter;
import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.AddressBook;
import seedu.address.model.ReadOnlyAddressBook;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.EnrollmentId;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentId;

/**
 * An Immutable AddressBook that is serializable to JSON format.
 */
@JsonRootName(value = "addressbook")
class JsonSerializableAddressBook {

    public static final String MESSAGE_DUPLICATE_PERSON = "Persons list contains duplicate person(s).";
    public static final String MESSAGE_DUPLICATE_STUDENT_ID = "Persons list contains duplicate student IDs.";
    public static final String MESSAGE_DUPLICATE_ENROLMENT =
            "A student has duplicate enrolments for the same class.";
    public static final String MESSAGE_CLASS_DETAILS_CONFLICT = "Students in the same class have different details.";

    private static final Logger logger = LogsCenter.getLogger(JsonSerializableAddressBook.class);

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
        int migratedEnrollments = 0;
        boolean hasLegacyNameVariants = false;
        for (JsonAdaptedTuitionClass adaptedClass : classes) {
            TuitionClass tuitionClass = adaptedClass.toModelType();
            if (addressBook.hasTuitionClass(tuitionClass.getClassId())
                    || addressBook.hasTuitionClassWithDetails(tuitionClass)) {
                throw new IllegalValueException("Classes list contains duplicate class IDs or class details.");
            }
            addressBook.addTuitionClass(tuitionClass);
        }
        java.util.Set<EnrollmentId> enrollmentIds = new java.util.HashSet<>();
        java.util.Set<StudentId> studentIds = new java.util.HashSet<>();
        for (JsonAdaptedPerson jsonAdaptedPerson : persons) {
            Person person = jsonAdaptedPerson.toModelType();
            if (!studentIds.add(person.getStudentId())) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_STUDENT_ID);
            }
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            hasLegacyNameVariants |= addressBook.getPersonList().stream()
                    .anyMatch(existing -> existing.getName().hasSameIdentityAs(person.getName()));
            java.util.Set<Lesson> explicitlyDetailedLessons = new java.util.HashSet<>();
            for (Lesson lesson : person.getLegacyLessons()) {
                if (jsonAdaptedPerson.hasExplicitClassDetails(lesson)) {
                    explicitlyDetailedLessons.add(lesson);
                }
            }
            java.util.Set<Enrollment> linkedEnrollments = new java.util.LinkedHashSet<>();
            for (Enrollment enrollment : person.getEnrollments()) {
                if (!enrollmentIds.add(enrollment.getEnrollmentId())) {
                    throw new IllegalValueException("Enrollments list contains duplicate enrollment IDs.");
                }
                if (!addressBook.hasTuitionClass(enrollment.getClassId())) {
                    throw new IllegalValueException("Enrollment refers to a class that does not exist: "
                            + enrollment.getClassId());
                }
                linkedEnrollments.add(enrollment.resolveWith(
                        addressBook.getTuitionClass(enrollment.getClassId())));
            }
            for (Lesson lesson : person.getLegacyLessons()) {
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
                    tuitionClass = new TuitionClass(id, lesson.getSubject(), lesson.getCost(), lesson.getSchedule(),
                            lesson.getDetails());
                    addressBook.addTuitionClass(tuitionClass);
                }
                if (explicitlyDetailedLessons.contains(lesson)
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
                        throw new IllegalValueException(MESSAGE_CLASS_DETAILS_CONFLICT);
                    }
                }
                Enrollment migrated = new Enrollment(person.getStudentId(), tuitionClass,
                        lesson.getCost(), lesson.getPaymentStatus());
                migratedEnrollments++;
                enrollmentIds.add(migrated.getEnrollmentId());
                linkedEnrollments.add(migrated);
            }
            long distinctClasses = linkedEnrollments.stream().map(Enrollment::getClassId).distinct().count();
            if (distinctClasses != linkedEnrollments.size()) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_ENROLMENT);
            }
            addressBook.addPerson(person.withEnrollments(linkedEnrollments));
        }
        int minimumNext = addressBook.getTuitionClassList().stream()
                .mapToInt(item -> item.getClassId().getSequence()).max().orElse(0) + 1;
        addressBook.setNextClassSequence(Math.max(minimumNext,
                nextClassSequence == null ? 1 : nextClassSequence));
        if (hasLegacyNameVariants) {
            logger.warning("Retained existing records with names differing only in case or spacing.");
        }
        logger.fine("Loaded " + addressBook.getPersonList().size() + " student(s), "
                + addressBook.getTuitionClassList().size() + " class(es); migrated "
                + migratedEnrollments + " legacy enrolment(s).");
        return addressBook;
    }

}
