package seedu.address.storage;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import seedu.address.commons.exceptions.IllegalValueException;
import seedu.address.model.person.Address;
import seedu.address.model.person.Cost;
import seedu.address.model.person.Email;
import seedu.address.model.person.Lesson;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.Subject;
import seedu.address.model.tag.Tag;

/**
 * Jackson-friendly version of {@link Person}.
 */
class JsonAdaptedPerson {

    public static final String MISSING_FIELD_MESSAGE_FORMAT = "Person's %s field is missing!";

    private final String name;
    private final String phone;
    private final String email;
    private final String address;
    private final List<JsonAdaptedTag> tags = new ArrayList<>();
    private final List<String> subjects = new ArrayList<>();
    private final String cost;
    private final String lesson;

    /**
     * Constructs an adapted person without additional student details.
     * Preserves compatibility with existing constructor calls.
     */
    public JsonAdaptedPerson(String name, String phone, String email,
            String address, List<JsonAdaptedTag> tags) {
        this(name, phone, email, address, tags, null, null, null);
    }

    /**
     * Constructs an adapted person from JSON properties.
     * Missing student details are allowed for older saved files.
     */
    @JsonCreator
    public JsonAdaptedPerson(
            @JsonProperty("name") String name,
            @JsonProperty("phone") String phone,
            @JsonProperty("email") String email,
            @JsonProperty("address") String address,
            @JsonProperty("tags") List<JsonAdaptedTag> tags,
            @JsonProperty("subjects") List<String> subjects,
            @JsonProperty("cost") String cost,
            @JsonProperty("lesson") String lesson) {
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cost = cost;
        this.lesson = lesson;

        if (tags != null) {
            this.tags.addAll(tags);
        }
        if (subjects != null) {
            this.subjects.addAll(subjects);
        }
    }

    /**
     * Converts a person into its JSON-friendly representation.
     */
    public JsonAdaptedPerson(Person source) {
        name = source.getName().fullName;
        phone = source.getPhone().value;
        email = source.getEmail().value;
        address = source.getAddress().value;

        tags.addAll(source.getTags().stream()
                .map(JsonAdaptedTag::new)
                .collect(Collectors.toList()));

        subjects.addAll(source.getSubjects().stream()
                .map(Subject::toString)
                .sorted()
                .collect(Collectors.toList()));

        cost = source.getCost().map(Cost::toString).orElse(null);
        lesson = source.getLesson().map(Lesson::toString).orElse(null);
    }

    /**
     * Converts the JSON-friendly representation into a Person.
     *
     * @throws IllegalValueException if any stored field violates its constraints.
     */
    public Person toModelType() throws IllegalValueException {
        final Set<Tag> modelTags = new HashSet<>();
        for (JsonAdaptedTag tag : tags) {
            modelTags.add(tag.toModelType());
        }

        if (name == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT, Name.class.getSimpleName()));
        }
        if (!Name.isValidName(name)) {
            throw new IllegalValueException(Name.MESSAGE_CONSTRAINTS);
        }
        final Name modelName = new Name(name);

        if (phone == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT, Phone.class.getSimpleName()));
        }
        if (!Phone.isValidPhone(phone)) {
            throw new IllegalValueException(Phone.MESSAGE_CONSTRAINTS);
        }
        final Phone modelPhone = new Phone(phone);

        if (email == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT, Email.class.getSimpleName()));
        }
        if (!Email.isValidEmail(email)) {
            throw new IllegalValueException(Email.MESSAGE_CONSTRAINTS);
        }
        final Email modelEmail = new Email(email);

        if (address == null) {
            throw new IllegalValueException(
                    String.format(MISSING_FIELD_MESSAGE_FORMAT, Address.class.getSimpleName()));
        }
        if (!Address.isValidAddress(address)) {
            throw new IllegalValueException(Address.MESSAGE_CONSTRAINTS);
        }
        final Address modelAddress = new Address(address);

        final Set<Subject> modelSubjects = new HashSet<>();
        for (String subject : subjects) {
            if (subject == null || !Subject.isValidSubject(subject)) {
                throw new IllegalValueException(Subject.MESSAGE_CONSTRAINTS);
            }
            modelSubjects.add(new Subject(subject));
        }

        if (cost != null && !Cost.isValidCost(cost)) {
            throw new IllegalValueException(Cost.MESSAGE_CONSTRAINTS);
        }
        final Optional<Cost> modelCost = Optional.ofNullable(cost).map(Cost::new);

        if (lesson != null && !Lesson.isValidLesson(lesson)) {
            throw new IllegalValueException(Lesson.MESSAGE_CONSTRAINTS);
        }
        final Optional<Lesson> modelLesson = Optional.ofNullable(lesson).map(Lesson::new);

        return new Person(
                modelName, modelPhone, modelEmail, modelAddress, modelTags,
                modelSubjects, modelCost, modelLesson);
    }
}
