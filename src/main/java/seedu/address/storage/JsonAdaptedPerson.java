package seedu.address.storage;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.EnrollmentId;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.StudentId;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";
    public static final String MESSAGE_DUPLICATE_ENROLLMENT_ID =
            "Person's enrollments contain duplicate enrollment IDs.";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    private final List<JsonAdaptedLesson> lessons = new ArrayList<>();
    private final List<String> subjects = new ArrayList<>();
    private final String cost;
    private final String lesson;
    private final boolean pinned;
    private final String guardianName;
    private final String guardianPhone;
    private final String guardianEmail;
    private final String studentId;

    /**
     * Constructs a {@code JsonAdaptedPerson} with the given person details.
     */
    @JsonCreator
    public JsonAdaptedPerson(@JsonProperty("name") String name, @JsonProperty("phone") String phone,
            @JsonProperty("email") String email, @JsonProperty("address") String address,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("lessons") List<JsonAdaptedLesson> lessons,
            @JsonProperty("subjects") List<String> subjects,
            @JsonProperty("cost") String cost,
            @JsonProperty("lesson") String lesson,
            @JsonProperty("guardianName") String guardianName,
            @JsonProperty("guardianPhone") String guardianPhone,
            @JsonProperty("guardianEmail") String guardianEmail, @JsonProperty("pinned") Boolean pinned,
            @JsonProperty("studentId") String studentId) {
        this.pinned = Boolean.TRUE.equals(pinned);
        this.studentId = studentId;
        this.guardianName = guardianName;
        this.guardianPhone = guardianPhone;
        this.guardianEmail = guardianEmail;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cost = cost;
        this.lesson = lesson;
        if (tags != null) {
            this.tags.addAll(tags);
        }
        if (lessons != null) {
            this.lessons.addAll(lessons);
        }
        if (subjects != null) {
            this.subjects.addAll(subjects);
        }
    }

    /** Retains the constructor used before stable student IDs were persisted. */
    public JsonAdaptedPerson(String name, String phone, String email, String address,
            List<JsonAdaptedTag> tags, List<JsonAdaptedLesson> lessons, List<String> subjects,
            String cost, String lesson, String guardianName, String guardianPhone,
            String guardianEmail, Boolean pinned) {
        this(name, phone, email, address, tags, lessons, subjects, cost, lesson,
                guardianName, guardianPhone, guardianEmail, pinned, null);
    }

    /** Retains the guardian-only constructor. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, List<JsonAdaptedTag> tags,
            List<JsonAdaptedLesson> lessons, List<String> subjects, String cost, String lesson,
            String guardianName, String guardianPhone, String guardianEmail) {
        this(name, phone, email, address, tags, lessons, subjects, cost, lesson,
                guardianName, guardianPhone, guardianEmail, false);
    }

    /** Retains the pin-aware constructor for records without guardians. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, List<JsonAdaptedTag> tags,
            List<JsonAdaptedLesson> lessons, List<String> subjects, String cost, String lesson, Boolean pinned) {
        this(name, phone, email, address, tags, lessons, subjects, cost, lesson, null, null, null, pinned);
    }

    /** Backwards-compatible constructor for records without guardian details. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, List<JsonAdaptedTag> tags,
            List<JsonAdaptedLesson> lessons, List<String> subjects, String cost, String lesson) {
        this(name, phone, email, address, tags, lessons, subjects, cost, lesson, null, null, null);
    }

    /** Backwards-compatible constructor for person data without lesson enrolments. */
    public JsonAdaptedPerson(String name, String phone, String email, String address, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, tags, null, null, null, null);
    }

    /** Constructor for records that use the previous student-detail properties. */
    public JsonAdaptedPerson(String name, String phone, String email, String address,
            List<JsonAdaptedTag> tags, List<String> subjects, String cost, String lesson) {
        this(name, phone, email, address, tags, null, subjects, cost, lesson);
    }

    /**
     * Converts a given {@code Person} into this class for Jackson use.
     */
    public JsonAdaptedPerson(Person source) {
        studentId = source.getStudentId().toString();
        pinned = source.isPinned();
        guardianName = source.getGuardianContact().name();
        guardianPhone = source.getGuardianContact().phone();
        guardianEmail = source.getGuardianContact().email();
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;
        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));
        lessons.addAll(source.getEnrollments().stream()
                .map(JsonAdaptedLesson::new).collect(Collectors.toList()));
        lessons.addAll(source.getLegacyLessons().stream()
                .map(JsonAdaptedLesson::new).collect(Collectors.toList()));
        subjects.addAll(source.getSubjects().stream().map(Subject::toString).sorted().toList());
        cost = source.getCost().map(Object::toString).orElse(null);
        lesson = source.getLesson().map(Object::toString).orElse(null);
    }

    /** Retains each supplied class field so omitted fields can inherit shared values independently. */
    JsonAdaptedLesson.ExplicitDetails getExplicitClassDetails(Lesson lesson) throws IllegalValueException {
        JsonAdaptedLesson.ExplicitDetails details = JsonAdaptedLesson.ExplicitDetails.NONE;
        for (JsonAdaptedLesson adapted : lessons) {
            if (!adapted.isAssociationRecord()
                    && adapted.hasExplicitDetails() && adapted.toModelType().equals(lesson)) {
                details = details.withProvided(adapted.getExplicitDetails());
            }
        }
        return details;
    }

    /**
     * Converts this Jackson-friendly adapted person object into the model's {@code Person} object.
     *
     * @throws IllegalValueException if there were any data constraints violated in the adapted person.
     */
    public Person toModelType() throws IllegalValueException {
        final List<Tag> personTags = new ArrayList<>();
        for (JsonAdaptedTag tag : tags) {
            if (tag == null) {
                throw new IllegalValueException("Tags list must not contain null entries.");
            }
            personTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        final Set<Tag> modelTags = new LinkedHashSet<>(personTags);
        if (modelTags.size() != personTags.size()) {
            throw new IllegalValueException(Tag.MESSAGE_DUPLICATE_TAGS);
        }
        final StudentId modelStudentId;
        if (studentId == null) {
            modelStudentId = StudentId.generate();
        } else if (!StudentId.isValidStudentId(studentId)) {
            throw new IllegalValueException(StudentId.MESSAGE_CONSTRAINTS);
        } else {
            modelStudentId = new StudentId(studentId);
        }
        final Set<Lesson> modelLessons = new LinkedHashSet<>();
        final Set<Enrollment> modelEnrollments = new LinkedHashSet<>();
        final Set<EnrollmentId> enrollmentIds = new LinkedHashSet<>();
        for (JsonAdaptedLesson adaptedLesson : lessons) {
            if (adaptedLesson == null) {
                throw new IllegalValueException("Lessons list must not contain null entries.");
            }
            if (adaptedLesson.isAssociationRecord()) {
                Enrollment enrollment = adaptedLesson.toEnrollmentModelType();
                if (!modelStudentId.equals(enrollment.getStudentId())) {
                    throw new IllegalValueException("Enrollment refers to a different student.");
                }
                if (!enrollmentIds.add(enrollment.getEnrollmentId())) {
                    throw new IllegalValueException(MESSAGE_DUPLICATE_ENROLLMENT_ID);
                }
                modelEnrollments.add(enrollment);
            } else {
                modelLessons.add(adaptedLesson.toModelType());
            }
        }

        final Set<Subject> modelSubjects = new LinkedHashSet<>();
        for (String subject : subjects) {
            if (subject == null || !Subject.isValidSubject(subject)) {
                throw new IllegalValueException(Subject.MESSAGE_CONSTRAINTS);
            }
            modelSubjects.add(new Subject(subject));
        }
        if (cost != null && !seedu.address.model.person.Cost.isValidCost(cost)) {
            throw new IllegalValueException(seedu.address.model.person.Cost.MESSAGE_CONSTRAINTS);
        }
        Optional<seedu.address.model.person.Cost> modelCost = Optional.ofNullable(cost)
                .map(seedu.address.model.person.Cost::new);
        if (lesson != null && !seedu.address.model.person.Lesson.isValidLesson(lesson)) {
            throw new IllegalValueException(seedu.address.model.person.Lesson.MESSAGE_CONSTRAINTS);
        }
        Optional<seedu.address.model.person.Lesson> modelLesson = Optional.ofNullable(lesson)
                .map(seedu.address.model.person.Lesson::new);

        if (modelLessons.isEmpty() && modelEnrollments.isEmpty()
                && !modelSubjects.isEmpty() && modelCost.isPresent() && modelLesson.isPresent()) {
            Person migrated = new Person(modelName, modelPhone, modelEmail, modelAddress, modelTags,
                    modelSubjects, modelCost, modelLesson);
            modelLessons.addAll(migrated.getLessons());
        }
        try {
            GuardianContact guardian = new GuardianContact(guardianName == null ? "" : guardianName,
                    guardianPhone == null ? "" : guardianPhone, guardianEmail == null ? "" : guardianEmail);
            return new Person(modelStudentId, modelName, modelPhone, modelEmail, modelAddress,
                    modelTags, modelLessons, modelEnrollments, modelSubjects, modelCost,
                    modelLesson, guardian, pinned);
        } catch (IllegalArgumentException e) {
            throw new IllegalValueException(e.getMessage());
        }
    }

}
