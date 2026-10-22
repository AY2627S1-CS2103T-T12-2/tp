package seedu.address.testutil;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.person.StudentId;
import seedu.address.model.tag.Tag;
import seedu.address.model.util.SampleDataUtil;

/**
 * A utility class to help with building Person objects.
 */
public class PersonBuilder {

    public static final String DEFAULT_NAME = "Amy Bee";
    public static final String DEFAULT_PHONE = "85355255";
    public static final String DEFAULT_EMAIL = "amy@gmail.com";
    public static final String DEFAULT_ADDRESS = "123, Jurong West Ave 6, #08-111";

    private GuardianContact guardianContact = GuardianContact.EMPTY;
    private StudentId studentId;
    private Name name;
    private Phone phone;
    private Email email;
    private Address address;
    private Set<Tag> tags;
    private Set<Lesson> lessons;
    private Set<Enrollment> enrollments;
    private boolean pinned;

    /**
     * Creates a {@code PersonBuilder} with the default details.
     */
    public PersonBuilder() {
        studentId = StudentId.generate();
        name = new Name(DEFAULT_NAME);
        phone = new Phone(DEFAULT_PHONE);
        email = new Email(DEFAULT_EMAIL);
        address = new Address(DEFAULT_ADDRESS);
        tags = new HashSet<>();
        lessons = new HashSet<>();
        enrollments = new HashSet<>();
        pinned = false;
    }

    /**
     * Initializes the PersonBuilder with the data of {@code personToCopy}.
     */
    public PersonBuilder(Person personToCopy) {
        studentId = personToCopy.getStudentId();
        guardianContact = personToCopy.getGuardianContact();
        name = personToCopy.getName();
        phone = personToCopy.getPhone();
        email = personToCopy.getEmail();
        address = personToCopy.getAddress();
        tags = new HashSet<>(personToCopy.getTags());
        lessons = new HashSet<>(personToCopy.getLegacyLessons());
        enrollments = new HashSet<>(personToCopy.getEnrollments());
        pinned = personToCopy.isPinned();
    }

    /**
     * Sets the {@code Name} of the {@code Person} that we are building.
     */
    public PersonBuilder withName(String name) {
        this.name = new Name(name);
        return this;
    }

    /** Sets the stable student ID of the {@code Person} being built. */
    public PersonBuilder withStudentId(StudentId studentId) {
        this.studentId = studentId;
        return this;
    }

    /**
     * Parses the {@code tags} into a {@code Set<Tag>} and sets it to the {@code Person} that we are building.
     */
    public PersonBuilder withTags(String ... tags) {
        this.tags = SampleDataUtil.getTagSet(tags);
        return this;
    }

    /**
     * Sets the {@code Address} of the {@code Person} that we are building.
     */
    public PersonBuilder withAddress(String address) {
        this.address = new Address(address);
        return this;
    }

    /**
     * Sets the {@code Phone} of the {@code Person} that we are building.
     */
    public PersonBuilder withPhone(String phone) {
        this.phone = new Phone(phone);
        return this;
    }

    /**
     * Sets the {@code Email} of the {@code Person} that we are building.
     */
    public PersonBuilder withEmail(String email) {
        this.email = new Email(email);
        return this;
    }

    /** Sets the person's lessons. */
    public PersonBuilder withLessons(Lesson ... lessons) {
        this.lessons = new HashSet<>(java.util.Arrays.asList(lessons));
        enrollments.clear();
        return this;
    }

    /** Sets the person's canonical enrollment associations. */
    public PersonBuilder withEnrollments(Enrollment ... enrollments) {
        this.enrollments = new HashSet<>(java.util.Arrays.asList(enrollments));
        lessons.clear();
        return this;
    }

    /** Sets the optional guardian details. */
    public PersonBuilder withGuardianContact(String name, String phone, String email) {
        guardianContact = new GuardianContact(name, phone, email);
        return this;
    }

    /** Sets whether the person is pinned. */
    public PersonBuilder withPinned(boolean pinned) {
        this.pinned = pinned;
        return this;
    }

    /** Builds the person with its stable ID, legacy lessons, and canonical associations. */
    public Person build() {
        return new Person(studentId, name, phone, email, address, tags, lessons, enrollments,
                Set.of(), Optional.empty(), Optional.empty(), guardianContact, pinned);
    }

}
