package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.tag.Tag;

/**
 * Represents a person and their student details in the address book.
 * Guarantees: fields are non-null, field values are validated, and the person is immutable.
 * Cost and lesson timing may be absent.
 */
public class Person {

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final Set<Tag> tags = new HashSet<>();
    private final Set<Subject> subjects;
    private final Optional<Cost> cost;
    private final Optional<Lesson> lesson;

    /**
     * Creates a person without additional student details.
     * Preserves compatibility with existing code that uses the original constructor.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, email, address, tags,
                Set.of(), Optional.empty(), Optional.empty());
    }

    /**
     * Creates a person with additional student details.
     * All arguments must be non-null. Cost and lesson timing may be empty optionals.
     * Defensive copies of tags and subjects prevent external modification.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags,
            Set<Subject> subjects, Optional<Cost> cost, Optional<Lesson> lesson) {
        requireAllNonNull(name, phone, email, address, tags, subjects, cost, lesson);

        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
        this.subjects = Set.copyOf(subjects);
        this.cost = cost;
        this.lesson = lesson;
    }

    public Name getName() {
        return name;
    }

    public Phone getPhone() {
        return phone;
    }

    public Email getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    /**
     * Returns an unmodifiable view of the person's tags.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns the person's immutable set of subjects.
     */
    public Set<Subject> getSubjects() {
        return subjects;
    }

    /**
     * Returns the hourly cost, or an empty optional if unspecified.
     */
    public Optional<Cost> getCost() {
        return cost;
    }

    /**
     * Returns the provisional lesson timing, or an empty optional if unspecified.
     */
    public Optional<Lesson> getLesson() {
        return lesson;
    }

    /**
     * Returns true if both persons have the same name.
     * Defines identity for duplicate detection independently of other details.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().equals(getName());
    }

    /**
     * Returns true if both persons have the same identity and data fields,
     * including their subjects, cost, and lesson timing.
     */
    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof Person otherPerson)) {
            return false;
        }

        return name.equals(otherPerson.name)
                && phone.equals(otherPerson.phone)
                && email.equals(otherPerson.email)
                && address.equals(otherPerson.address)
                && tags.equals(otherPerson.tags)
                && subjects.equals(otherPerson.subjects)
                && cost.equals(otherPerson.cost)
                && lesson.equals(otherPerson.lesson);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, phone, email, address, tags, subjects, cost, lesson);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("tags", tags)
                .add("subjects", subjects)
                .add("cost", cost)
                .add("lesson", lesson)
                .toString();
    }
}