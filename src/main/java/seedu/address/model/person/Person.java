package seedu.address.model.person;

import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import seedu.address.commons.util.ToStringBuilder;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.PaymentStatus;
import seedu.address.model.tag.Tag;

/**
 * Represents a Person in the address book.
 * Guarantees: details are present and not null, field values are validated, immutable.
 */
public class Person {

    private final GuardianContact guardianContact;

    // Identity fields
    private final Name name;
    private final Phone phone;
    private final Email email;

    // Data fields
    private final Address address;
    private final Set<Tag> tags = new LinkedHashSet<>();
    private final Set<Lesson> lessons = new LinkedHashSet<>();
    private final Set<Subject> legacySubjects;
    private final Optional<Cost> legacyCost;
    private final Optional<seedu.address.model.person.Lesson> legacyLesson;
    private final boolean pinned;

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags) {
        this(name, phone, email, address, tags, Collections.emptySet());
    }

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags, Set<Lesson> lessons) {
        this(name, phone, email, address, tags, lessons, false);
    }

    /**
     * Every field must be present and not null.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags, Set<Lesson> lessons,
            boolean pinned) {
        this(name, phone, email, address, tags, lessons, Set.of(), Optional.empty(), Optional.empty(), pinned);
    }

    /**
     * Creates a person while retaining student fields from the previous storage model.
     * The legacy fields remain available until all saved data has been migrated.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags, Set<Lesson> lessons,
            Set<Subject> legacySubjects, Optional<Cost> legacyCost,
            Optional<seedu.address.model.person.Lesson> legacyLesson) {
        this(name, phone, email, address, tags, lessons, legacySubjects, legacyCost, legacyLesson, false);
    }

    /**
     * Creates a person while retaining student fields from the previous storage model and its pin state.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags, Set<Lesson> lessons,
            Set<Subject> legacySubjects, Optional<Cost> legacyCost,
            Optional<seedu.address.model.person.Lesson> legacyLesson, boolean pinned) {
        this(name, phone, email, address, tags, lessons, legacySubjects, legacyCost, legacyLesson,
                GuardianContact.EMPTY, pinned);
    }

    /** Creates a person retaining both guardian details and pin state. */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags, Set<Lesson> lessons,
            Set<Subject> legacySubjects, Optional<Cost> legacyCost,
            Optional<seedu.address.model.person.Lesson> legacyLesson, GuardianContact guardianContact, boolean pinned) {
        requireAllNonNull(name, phone, email, address, tags, lessons,
                legacySubjects, legacyCost, legacyLesson, guardianContact);
        this.guardianContact = guardianContact;
        this.name = name;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.tags.addAll(tags);
        this.lessons.addAll(lessons);
        this.legacySubjects = Set.copyOf(legacySubjects);
        this.legacyCost = legacyCost;
        this.legacyLesson = legacyLesson;
        this.pinned = pinned;
    }

    /**
     * Creates a person using the student fields from the previous model.
     */
    public Person(Name name, Phone phone, Email email, Address address, Set<Tag> tags,
            Set<Subject> subjects, Optional<Cost> cost,
            Optional<seedu.address.model.person.Lesson> lesson) {
        this(name, phone, email, address, tags, migrateLegacyLessons(subjects, cost, lesson),
                subjects, cost, lesson);
    }

    private static Set<Lesson> migrateLegacyLessons(Set<Subject> subjects, Optional<Cost> cost,
            Optional<seedu.address.model.person.Lesson> lesson) {
        requireAllNonNull(subjects, cost, lesson);
        if (subjects.isEmpty() || cost.isEmpty() || lesson.isEmpty()) {
            return Set.of();
        }

        String legacyTiming = lesson.get().toString();
        String startText = legacyTiming.substring(legacyTiming.length() - 4);
        LocalTime start = LocalTime.parse(startText, DateTimeFormatter.ofPattern("HHmm"));
        LocalTime end = start.plusHours(1);
        if (!end.isAfter(start)) {
            end = LocalTime.of(23, 59);
        }
        if (!end.isAfter(start)) {
            return Set.of();
        }

        String migratedTiming = legacyTiming + "-" + end.format(DateTimeFormatter.ofPattern("HHmm"));
        Set<Lesson> migrated = new LinkedHashSet<>();
        for (Subject subject : subjects) {
            migrated.add(new Lesson(new seedu.address.model.lesson.Subject(subject.toString()),
                    new seedu.address.model.lesson.Cost(cost.get().toString()),
                    new seedu.address.model.lesson.LessonTiming(migratedTiming)));
        }
        return migrated;
    }

    public GuardianContact getGuardianContact() {
        return guardianContact;
    }

    /** Returns a copy with only its guardian details replaced. */
    public Person withGuardianContact(GuardianContact updatedContact) {
        return new Person(name, phone, email, address, tags, lessons,
                legacySubjects, legacyCost, legacyLesson, updatedContact, pinned);
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
     * Returns an immutable tag set, which throws {@code UnsupportedOperationException}
     * if modification is attempted.
     */
    public Set<Tag> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Returns a copy of this person with {@code updatedTags}, retaining every non-tag field.
     */
    public Person withTags(Set<Tag> updatedTags) {
        requireAllNonNull(updatedTags);
        return new Person(name, phone, email, address, updatedTags, lessons,
                legacySubjects, legacyCost, legacyLesson, guardianContact, pinned);
    }

    /** Returns the immutable set of lessons in which this person is enrolled. */
    public Set<Lesson> getLessons() {
        return Collections.unmodifiableSet(lessons);
    }

    /** Returns the total cost of this person's unpaid lesson enrolments. */
    public BigDecimal getOutstandingBalance() {
        return lessons.stream()
                .filter(lesson -> lesson.getPaymentStatus() == PaymentStatus.UNPAID)
                .map(lesson -> lesson.getCost().value)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .stripTrailingZeros();
    }

    /** Returns a copy of this person with {@code updatedLessons} replacing the current lessons. */
    public Person withLessons(Set<Lesson> updatedLessons) {
        return new Person(name, phone, email, address, tags, updatedLessons,
                legacySubjects, legacyCost, legacyLesson, guardianContact, pinned);
    }

    /** Returns whether this person is pinned. */
    public boolean isPinned() {
        return pinned;
    }

    /** Returns a copy of this person with the specified pin state. */
    public Person withPinned(boolean pinned) {
        return new Person(name, phone, email, address, tags, lessons,
                legacySubjects, legacyCost, legacyLesson, guardianContact, pinned);
    }

    /** Returns subjects retained from the previous student model. */
    public Set<Subject> getSubjects() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(legacySubjects));
    }

    /** Returns the cost retained from the previous student model. */
    public Optional<Cost> getCost() {
        return legacyCost;
    }

    /** Returns the lesson start retained from the previous student model. */
    public Optional<seedu.address.model.person.Lesson> getLesson() {
        return legacyLesson;
    }

    /**
     * Returns true if both persons have the same name.
     * This defines a weaker notion of equality between two persons.
     */
    public boolean isSamePerson(Person otherPerson) {
        if (otherPerson == this) {
            return true;
        }

        return otherPerson != null
                && otherPerson.getName().equals(getName());
    }

    /**
     * Returns true if both persons have the same identity and data fields.
     * This defines a stronger notion of equality between two persons.
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
                && lessons.equals(otherPerson.lessons)
                && legacySubjects.equals(otherPerson.legacySubjects)
                && legacyCost.equals(otherPerson.legacyCost)
                && legacyLesson.equals(otherPerson.legacyLesson)
                && pinned == otherPerson.pinned && guardianContact.equals(otherPerson.guardianContact);
    }

    @Override
    public int hashCode() {
        // use this method for custom fields hashing instead of implementing your own
        return Objects.hash(name, phone, email, address, tags, lessons,
                legacySubjects, legacyCost, legacyLesson, guardianContact, pinned);
    }

    @Override
    public String toString() {
        ToStringBuilder builder = new ToStringBuilder(this)
                .add("name", name)
                .add("phone", phone)
                .add("email", email)
                .add("address", address)
                .add("tags", tags);
        if (!lessons.isEmpty() && legacySubjects.isEmpty() && legacyCost.isEmpty() && legacyLesson.isEmpty()) {
            builder.add("lessons", lessons);
        }
        builder.add("subjects", legacySubjects)
                .add("cost", legacyCost)
                .add("lesson", legacyLesson)
                .add("pinned", pinned);
        if (!guardianContact.isEmpty()) {
            builder.add("guardian", guardianContact);
        }
        return builder.toString();
    }

}
