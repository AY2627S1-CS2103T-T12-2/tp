package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.logic.parser.CliSyntax.PREFIX_ADDRESS;
import static seedu.address.logic.parser.CliSyntax.PREFIX_EMAIL;
import static seedu.address.logic.parser.CliSyntax.PREFIX_NAME;
import static seedu.address.logic.parser.CliSyntax.PREFIX_PHONE;
import static seedu.address.logic.parser.CliSyntax.PREFIX_TAG;
import static seedu.address.model.Model.PREDICATE_SHOW_ALL_PERSONS;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.CollectionUtil;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.lesson.ClassDetails;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Address;
import seedu.address.model.person.Email;
import seedu.address.model.person.GuardianContact;
import seedu.address.model.person.Name;
import seedu.address.model.person.Person;
import seedu.address.model.person.Phone;
import seedu.address.model.tag.Tag;

/**
 * Edits the details of an existing person in the address book.
 */
public class EditCommand extends Command {

    public static final String COMMAND_WORD = "edit";

    public static final String MESSAGE_USAGE = COMMAND_WORD + ": Edits the details of the person identified "
            + "by the index number used in the displayed person list. "
            + "Existing values will be overwritten by the input values.\n"
            + "Parameters: INDEX (must be a positive integer) "
            + "[" + PREFIX_NAME + "NAME] "
            + "[" + PREFIX_PHONE + "PHONE] "
            + "[" + PREFIX_EMAIL + "EMAIL] "
            + "[" + PREFIX_ADDRESS + "ADDRESS] "
            + "[" + PREFIX_TAG + "TAG]... [gn/GUARDIAN_NAME] [gp/GUARDIAN_PHONE] [ge/GUARDIAN_EMAIL] "
            + "[sub/SUBJECT c/DAYHHmm-HHmm [mode/online|offline] [link/URL] [location/LOCATION] [notes/NOTES]]\n"
            + "Example: " + COMMAND_WORD + " 1 "
            + PREFIX_PHONE + "91234567 "
            + PREFIX_EMAIL + "johndoe@example.com";

    public static final String MESSAGE_EDIT_PERSON_SUCCESS = "Edited person: %1$s";
    public static final String MESSAGE_NOT_EDITED = "At least one field to edit must be provided.";
    public static final String MESSAGE_DUPLICATE_PERSON = "This person already exists in the address book.";

    public static final String MESSAGE_CLASS_IDENTITY_REQUIRED =
            "To edit class fields, supply both sub/SUBJECT and c/DAYHHmm-HHmm and at least one of "
                    + "mode/, link/, location/, or notes/.";
    public static final String MESSAGE_CLASS_NOT_ENROLLED = "This student is not enrolled in the specified class.";

    private final Index index;
    private final EditPersonDescriptor editPersonDescriptor;

    /**
     * @param index of the person in the filtered person list to edit
     * @param editPersonDescriptor details to edit the person with
     */
    public EditCommand(Index index, EditPersonDescriptor editPersonDescriptor) {
        requireNonNull(index);
        requireNonNull(editPersonDescriptor);

        this.index = index;
        this.editPersonDescriptor = new EditPersonDescriptor(editPersonDescriptor);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (index.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToEdit = lastShownList.get(index.getZeroBased());
        Person editedPerson = createEditedPerson(personToEdit, editPersonDescriptor);

        if (!personToEdit.isSamePerson(editedPerson) && model.hasPerson(editedPerson)) {
            throw new CommandException(MESSAGE_DUPLICATE_PERSON);
        }

        Map<Person, Person> replacements = new LinkedHashMap<>();
        replacements.put(personToEdit, editedPerson);
        String classFeedback = "";
        if (!editPersonDescriptor.getClassDetailsUpdate().isEmpty()) {
            Subject subject = editPersonDescriptor.getClassSubject()
                    .orElseThrow(() -> new CommandException(MESSAGE_CLASS_IDENTITY_REQUIRED));
            LessonTiming timing = editPersonDescriptor.getClassTiming()
                    .orElseThrow(() -> new CommandException(MESSAGE_CLASS_IDENTITY_REQUIRED));
            Lesson enrolment = personToEdit.getLessons().stream()
                    .filter(lesson -> lesson.isSameClass(subject, timing)).findFirst()
                    .orElseThrow(() -> new CommandException(MESSAGE_CLASS_NOT_ENROLLED));
            ClassDetails details;
            try {
                details = editPersonDescriptor.getClassDetailsUpdate().applyTo(enrolment.getDetails());
            } catch (IllegalArgumentException e) {
                throw new CommandException(e.getMessage());
            }
            for (Person person : List.copyOf(model.getAddressBook().getPersonList())) {
                if (person.getLessons().stream().anyMatch(lesson -> lesson.isSameClass(subject, timing))) {
                    Person base = person.equals(personToEdit) ? editedPerson : person;
                    Set<Lesson> lessons = base.getLessons().stream()
                            .map(lesson -> lesson.isSameClass(subject, timing) ? lesson.withDetails(details) : lesson)
                            .collect(Collectors.toSet());
                    replacements.put(person, base.withLessons(lessons));
                }
            }
            editedPerson = replacements.get(personToEdit);
            classFeedback = "; Class " + subject + " (" + timing + "): " + details;
        }
        replacements.forEach(model::setPerson);
        model.updateFilteredPersonList(PREDICATE_SHOW_ALL_PERSONS);
        return new CommandResult(String.format(MESSAGE_EDIT_PERSON_SUCCESS, Messages.format(editedPerson))
                + classFeedback);
    }

    /**
     * Creates and returns a {@code Person} with the details of {@code personToEdit}
     * edited with {@code editPersonDescriptor}.
     */
    private static Person createEditedPerson(Person personToEdit, EditPersonDescriptor editPersonDescriptor) {
        assert personToEdit != null;

        Name updatedName = editPersonDescriptor.getName().orElse(personToEdit.getName());
        Phone updatedPhone = editPersonDescriptor.getPhone().orElse(personToEdit.getPhone());
        Email updatedEmail = editPersonDescriptor.getEmail().orElse(personToEdit.getEmail());
        Address updatedAddress = editPersonDescriptor.getAddress().orElse(personToEdit.getAddress());
        Set<Tag> updatedTags = editPersonDescriptor.getTags().orElse(personToEdit.getTags());

        return new Person(updatedName, updatedPhone, updatedEmail, updatedAddress, updatedTags,
                personToEdit.getLessons(), personToEdit.getSubjects(), personToEdit.getCost(),
                personToEdit.getLesson(), new GuardianContact(
                        editPersonDescriptor.getGuardianName().orElse(personToEdit.getGuardianContact().name()),
                        editPersonDescriptor.getGuardianPhone().orElse(personToEdit.getGuardianContact().phone()),
                        editPersonDescriptor.getGuardianEmail().orElse(personToEdit.getGuardianContact().email())));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof EditCommand otherEditCommand)) {
            return false;
        }

        return index.equals(otherEditCommand.index)
                && editPersonDescriptor.equals(otherEditCommand.editPersonDescriptor);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("index", index)
                .add("editPersonDescriptor", editPersonDescriptor)
                .toString();
    }

    /**
     * Stores the details to edit the person with. Each non-empty field value will replace the
     * corresponding field value of the person.
     */
    public static class EditPersonDescriptor {
        private Subject classSubject;
        private LessonTiming classTiming;
        private ClassDetailsUpdate classDetailsUpdate = ClassDetailsUpdate.NONE;
        private String guardianName;
        private String guardianPhone;
        private String guardianEmail;
        private Name name;
        private Phone phone;
        private Email email;
        private Address address;
        private Set<Tag> tags;

        public EditPersonDescriptor() {}

        /**
         * Copy constructor.
         * A defensive copy of {@code tags} is used internally.
         */
        public EditPersonDescriptor(EditPersonDescriptor toCopy) {
            setClassSubject(toCopy.classSubject);
            setClassTiming(toCopy.classTiming);
            setClassDetailsUpdate(toCopy.classDetailsUpdate);
            setName(toCopy.name);
            setPhone(toCopy.phone);
            setEmail(toCopy.email);
            setAddress(toCopy.address);
            setTags(toCopy.tags);
            setGuardianName(toCopy.guardianName);
            setGuardianPhone(toCopy.guardianPhone);
            setGuardianEmail(toCopy.guardianEmail);
        }

        /**
         * Returns true if at least one field is edited.
         */
        public boolean isAnyFieldEdited() {
            return CollectionUtil.isAnyNonNull(name, phone, email, address, tags,
                    guardianName, guardianPhone, guardianEmail) || !classDetailsUpdate.isEmpty();
        }

        public void setClassSubject(Subject subject) {
            classSubject = subject;
        }

        public Optional<Subject> getClassSubject() {
            return Optional.ofNullable(classSubject);
        }

        public void setClassTiming(LessonTiming timing) {
            classTiming = timing;
        }

        public Optional<LessonTiming> getClassTiming() {
            return Optional.ofNullable(classTiming);
        }

        public void setClassDetailsUpdate(ClassDetailsUpdate update) {
            classDetailsUpdate = requireNonNull(update);
        }

        public ClassDetailsUpdate getClassDetailsUpdate() {
            return classDetailsUpdate;
        }

        public void setGuardianName(String guardianName) {
            this.guardianName = guardianName;
        }

        public Optional<String> getGuardianName() {
            return Optional.ofNullable(guardianName);
        }

        public void setGuardianPhone(String guardianPhone) {
            this.guardianPhone = guardianPhone;
        }

        public Optional<String> getGuardianPhone() {
            return Optional.ofNullable(guardianPhone);
        }

        public void setGuardianEmail(String guardianEmail) {
            this.guardianEmail = guardianEmail;
        }

        public Optional<String> getGuardianEmail() {
            return Optional.ofNullable(guardianEmail);
        }

        public void setName(Name name) {
            this.name = name;
        }

        public Optional<Name> getName() {
            return Optional.ofNullable(name);
        }

        public void setPhone(Phone phone) {
            this.phone = phone;
        }

        public Optional<Phone> getPhone() {
            return Optional.ofNullable(phone);
        }

        public void setEmail(Email email) {
            this.email = email;
        }

        public Optional<Email> getEmail() {
            return Optional.ofNullable(email);
        }

        public void setAddress(Address address) {
            this.address = address;
        }

        public Optional<Address> getAddress() {
            return Optional.ofNullable(address);
        }

        /**
         * Sets {@code tags} to this object's {@code tags}.
         * A defensive copy of {@code tags} is used internally.
         */
        public void setTags(Set<Tag> tags) {
            this.tags = (tags != null) ? new HashSet<>(tags) : null;
        }

        /**
         * Returns an unmodifiable tag set, which throws {@code UnsupportedOperationException}
         * if modification is attempted.
         * Returns {@code Optional#empty()} if {@code tags} is null.
         */
        public Optional<Set<Tag>> getTags() {
            return (tags != null) ? Optional.of(Collections.unmodifiableSet(tags)) : Optional.empty();
        }

        @Override
        public boolean equals(Object other) {
            if (other == this) {
                return true;
            }

            // instanceof handles nulls
            if (!(other instanceof EditPersonDescriptor otherEditPersonDescriptor)) {
                return false;
            }

            return Objects.equals(name, otherEditPersonDescriptor.name)
                    && Objects.equals(phone, otherEditPersonDescriptor.phone)
                    && Objects.equals(email, otherEditPersonDescriptor.email)
                    && Objects.equals(address, otherEditPersonDescriptor.address)
                    && Objects.equals(tags, otherEditPersonDescriptor.tags)
                    && Objects.equals(guardianName, otherEditPersonDescriptor.guardianName)
                    && Objects.equals(guardianPhone, otherEditPersonDescriptor.guardianPhone)
                    && Objects.equals(guardianEmail, otherEditPersonDescriptor.guardianEmail)
                    && Objects.equals(classSubject, otherEditPersonDescriptor.classSubject)
                    && Objects.equals(classTiming, otherEditPersonDescriptor.classTiming)
                    && classDetailsUpdate.equals(otherEditPersonDescriptor.classDetailsUpdate);
        }

        @Override
        public String toString() {
            return new ToStringBuilder(this)
                    .add("name", name)
                    .add("phone", phone)
                    .add("email", email)
                    .add("address", address)
                    .add("tags", tags)
                    .toString();
        }
    }
}
