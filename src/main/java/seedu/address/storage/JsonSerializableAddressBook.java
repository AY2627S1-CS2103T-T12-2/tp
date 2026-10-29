package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import seedu.address.model.lesson.ClassId;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.EnrollmentId;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.person.Person;
import seedu.address.model.person.StudentId;
import seedu.address.storage.JsonAdaptedLesson.ExplicitDetails;

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
            if (adaptedClass == null) {
                throw new IllegalValueException("Classes list must not contain null entries.");
            }
            TuitionClass tuitionClass = adaptedClass.toModelType();
            if (addressBook.hasTuitionClass(tuitionClass.getClassId())
                    || addressBook.hasTuitionClassWithDetails(tuitionClass)) {
                throw new IllegalValueException("Classes list contains duplicate class IDs or class details.");
            }
            addressBook.addTuitionClass(tuitionClass);
        }
        Set<EnrollmentId> enrollmentIds = new HashSet<>();
        Set<StudentId> studentIds = new HashSet<>();
        List<LoadedStudent> loadedStudents = loadStudents(addressBook);
        reconcileLegacyClassDetails(addressBook, loadedStudents);
        for (LoadedStudent loadedStudent : loadedStudents) {
            Person person = loadedStudent.person();
            boolean hasMigratedLegacyDetails = !person.getLegacyLessons().isEmpty()
                    || (!person.getEnrollments().isEmpty() && person.hasMigratableLegacyLessonDetails());
            if (!studentIds.add(person.getStudentId())) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_STUDENT_ID);
            }
            if (addressBook.hasPerson(person)) {
                throw new IllegalValueException(MESSAGE_DUPLICATE_PERSON);
            }
            hasLegacyNameVariants |= addressBook.getPersonList().stream()
                    .anyMatch(existing -> existing.getName().hasSameIdentityAs(person.getName()));
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
                TuitionClass tuitionClass = resolveLegacyClass(addressBook, lesson);
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
            Person restored = hasMigratedLegacyDetails
                    ? person.withMigratedEnrollments(linkedEnrollments)
                    : person.withEnrollments(linkedEnrollments);
            addressBook.addPerson(restored);
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

    /** Reads legacy detail provenance before any students are linked to the class catalogue. */
    private List<LoadedStudent> loadStudents(AddressBook addressBook) throws IllegalValueException {
        List<LoadedStudent> loadedStudents = new ArrayList<>();
        for (JsonAdaptedPerson adapted : persons) {
            if (adapted == null) {
                throw new IllegalValueException("Persons list must not contain null entries.");
            }
            Person person = adapted.toModelType();
            Map<Lesson, ExplicitDetails> explicitDetails = new HashMap<>();
            for (Lesson lesson : person.getLegacyLessons()) {
                explicitDetails.put(lesson, adapted.getExplicitClassDetails(lesson));
            }
            for (Enrollment enrollment : person.getEnrollments()) {
                if (!addressBook.hasTuitionClass(enrollment.getClassId())) {
                    throw new IllegalValueException("Enrollment refers to a class that does not exist: "
                            + enrollment.getClassId());
                }
            }
            loadedStudents.add(new LoadedStudent(person, explicitDetails));
        }
        return loadedStudents;
    }

    /** Resolves omitted legacy details independently of record order, retaining explicit conflicts. */
    private static void reconcileLegacyClassDetails(AddressBook addressBook, List<LoadedStudent> students)
            throws IllegalValueException {
        Map<ClassId, ExplicitDetails> explicitClasses = new HashMap<>();
        addressBook.getTuitionClassList().stream()
                .filter(item -> !item.getDetails().equals(ClassDetails.EMPTY))
                .forEach(item -> explicitClasses.put(item.getClassId(), ExplicitDetails.complete(item.getDetails())));
        students.stream().flatMap(student -> student.person().getEnrollments().stream())
                .map(Enrollment::getClassId).forEach(id -> explicitClasses.put(id,
                        ExplicitDetails.complete(addressBook.getTuitionClass(id).getDetails())));
        for (LoadedStudent student : students) {
            for (Lesson lesson : student.person().getLegacyLessons()) {
                TuitionClass tuitionClass = resolveLegacyClass(addressBook, lesson);
                ExplicitDetails previous = explicitClasses.getOrDefault(
                        tuitionClass.getClassId(), ExplicitDetails.NONE);
                ExplicitDetails supplied = student.explicitDetails().get(lesson);
                if (previous.conflictsWith(supplied)) {
                    throw new IllegalValueException(MESSAGE_CLASS_DETAILS_CONFLICT);
                }
                ExplicitDetails combined = previous.withProvided(supplied);
                ClassDetails details;
                try {
                    details = combined.applyTo(tuitionClass.getDetails());
                } catch (IllegalArgumentException exception) {
                    throw new IllegalValueException(MESSAGE_CLASS_DETAILS_CONFLICT);
                }
                explicitClasses.put(tuitionClass.getClassId(), combined);
                if (!tuitionClass.getDetails().equals(details)) {
                    addressBook.setTuitionClass(tuitionClass, tuitionClass.withDetails(details));
                }
            }
        }
    }

    private static TuitionClass resolveLegacyClass(AddressBook addressBook, Lesson lesson) {
        if (lesson.getClassId() != null && addressBook.hasTuitionClass(lesson.getClassId())) {
            return addressBook.getTuitionClass(lesson.getClassId());
        }
        TuitionClass existing = addressBook.getTuitionClassList().stream()
                .filter(item -> item.getSubject().equals(lesson.getSubject())
                        && item.getSchedule().equals(lesson.getSchedule()))
                .findFirst().orElse(null);
        if (existing != null) {
            return existing;
        }
        ClassId id = lesson.getClassId() == null ? addressBook.allocateClassId() : lesson.getClassId();
        TuitionClass created = new TuitionClass(id, lesson.getSubject(), lesson.getCost(), lesson.getSchedule(),
                lesson.getDetails());
        addressBook.addTuitionClass(created);
        return created;
    }

    private record LoadedStudent(Person person, Map<Lesson, ExplicitDetails> explicitDetails) { }

}
