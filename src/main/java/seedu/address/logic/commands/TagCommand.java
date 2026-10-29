package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;
import static seedu.address.commons.util.CollectionUtil.requireAllNonNull;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import seedu.address.commons.core.index.Index;
import seedu.address.commons.util.ToStringBuilder;
import seedu.address.logic.Messages;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.model.Model;
import seedu.address.model.person.Person;
import seedu.address.model.tag.Tag;

/**
 * Adds tags to one or more persons identified using their displayed indexes.
 */
public class TagCommand extends Command {

    public static final String COMMAND_WORD = "tag";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds tags to one or more students identified by displayed indexes.\n"
            + "Single-student format: INDEX TAG [MORE_TAGS]\n"
            + "Batch format: STUDENT_INDEX [MORE_INDEXES] t/TAG\n"
            + "Examples:\n"
            + COMMAND_WORD + " 3 exam-prep needs-follow-up\n"
            + COMMAND_WORD + " 1 3 5 t/math-2026";

    public static final String MESSAGE_TAG_PERSON_SUCCESS = "Added tag(s) to %1$s: %2$s";
    public static final String MESSAGE_DUPLICATE_TAG = "One or more tags already exist for this student.";
    public static final String MESSAGE_DUPLICATE_INDEXES = "Student indexes must not be repeated.";
    public static final String MESSAGE_BATCH_MULTIPLE_TAGS =
            "Batch mode accepts exactly one tag. Use: tag STUDENT_INDEX [MORE_INDEXES] t/TAG.";
    public static final String MESSAGE_BATCH_SUCCESS =
            "Added tag '%1$s' to: %2$s. Already tagged: %3$s. At tag limit: %4$s.";
    public static final String MESSAGE_BATCH_NO_CHANGE =
            "No changes were made for tag '%1$s'. Already tagged: %2$s. At tag limit: %3$s.";
    public static final String MESSAGE_NO_STUDENTS = "None";

    private final List<Index> targetIndexes;
    private final Set<Tag> tagsToAdd;
    private final boolean isBatchCommand;

    /**
     * Creates a command that adds {@code tagsToAdd} to the person at {@code targetIndex}.
     */
    public TagCommand(Index targetIndex, Set<Tag> tagsToAdd) {
        requireNonNull(targetIndex);
        requireNonNull(tagsToAdd);
        this.targetIndexes = List.of(targetIndex);
        this.tagsToAdd = new LinkedHashSet<>(tagsToAdd);
        isBatchCommand = false;
    }

    /**
     * Creates a batch command that adds {@code tagToAdd} to persons at {@code targetIndexes}.
     */
    public TagCommand(List<Index> targetIndexes, Tag tagToAdd) {
        requireAllNonNull(targetIndexes);
        checkArgument(!targetIndexes.isEmpty(), "At least one student index must be provided.");
        checkArgument(targetIndexes.stream().map(Index::getZeroBased).distinct().count() == targetIndexes.size(),
                MESSAGE_DUPLICATE_INDEXES);
        this.targetIndexes = List.copyOf(targetIndexes);
        this.tagsToAdd = Set.of(requireNonNull(tagToAdd));
        isBatchCommand = true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndexes.stream().anyMatch(index -> index.getZeroBased() >= lastShownList.size())) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        List<Person> selectedPersons = targetIndexes.stream()
                .map(index -> lastShownList.get(index.getZeroBased()))
                .toList();
        return isBatchCommand
                ? executeBatch(model, selectedPersons)
                : executeSingle(model, selectedPersons.get(0));
    }

    private CommandResult executeSingle(Model model, Person personToTag) throws CommandException {
        Set<Tag> updatedTags = new LinkedHashSet<>(personToTag.getTags());

        if (tagsToAdd.stream().anyMatch(updatedTags::contains)) {
            throw new CommandException(MESSAGE_DUPLICATE_TAG);
        }
        if (updatedTags.size() + tagsToAdd.size() > Tag.MAX_TAGS_PER_PERSON) {
            throw new CommandException(Tag.MESSAGE_TAG_LIMIT);
        }

        updatedTags.addAll(tagsToAdd);
        Person taggedPerson = personToTag.withTags(updatedTags);
        model.setPerson(personToTag, taggedPerson);

        return new CommandResult(String.format(MESSAGE_TAG_PERSON_SUCCESS,
                personToTag.getName(), formatTags(tagsToAdd)));
    }

    private CommandResult executeBatch(Model model, List<Person> selectedPersons) {
        Tag tagToAdd = tagsToAdd.iterator().next();
        List<Person> personsToTag = new ArrayList<>();
        List<Person> alreadyTaggedPersons = new ArrayList<>();
        List<Person> personsAtTagLimit = new ArrayList<>();

        for (Person person : selectedPersons) {
            if (person.getTags().contains(tagToAdd)) {
                alreadyTaggedPersons.add(person);
            } else if (person.getTags().size() >= Tag.MAX_TAGS_PER_PERSON) {
                personsAtTagLimit.add(person);
            } else {
                personsToTag.add(person);
            }
        }

        Map<Person, Person> replacements = new LinkedHashMap<>();
        for (Person person : personsToTag) {
            Set<Tag> updatedTags = new LinkedHashSet<>(person.getTags());
            updatedTags.add(tagToAdd);
            replacements.put(person, person.withTags(updatedTags));
        }
        if (!replacements.isEmpty()) {
            model.setPersons(replacements);
        }

        return createBatchResult(tagToAdd, personsToTag, alreadyTaggedPersons, personsAtTagLimit);
    }

    private static CommandResult createBatchResult(Tag tagToAdd, List<Person> personsToTag,
            List<Person> alreadyTaggedPersons, List<Person> personsAtTagLimit) {
        if (personsToTag.isEmpty()) {
            return new CommandResult(String.format(MESSAGE_BATCH_NO_CHANGE, tagToAdd.tagName,
                    formatPersons(alreadyTaggedPersons), formatPersons(personsAtTagLimit)));
        }
        return new CommandResult(String.format(MESSAGE_BATCH_SUCCESS, tagToAdd.tagName,
                formatPersons(personsToTag), formatPersons(alreadyTaggedPersons),
                formatPersons(personsAtTagLimit)));
    }

    private static String formatTags(Set<Tag> tags) {
        return tags.stream().map(tag -> tag.tagName).collect(Collectors.joining(", "));
    }

    private static String formatPersons(List<Person> persons) {
        if (persons.isEmpty()) {
            return MESSAGE_NO_STUDENTS;
        }
        return persons.stream().map(person -> person.getName().toString()).collect(Collectors.joining(", "));
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TagCommand otherTagCommand)) {
            return false;
        }

        return targetIndexes.equals(otherTagCommand.targetIndexes)
                && tagsToAdd.equals(otherTagCommand.tagsToAdd)
                && isBatchCommand == otherTagCommand.isBatchCommand;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndexes", targetIndexes)
                .add("tagsToAdd", tagsToAdd)
                .add("isBatchCommand", isBatchCommand)
                .toString();
    }
}
