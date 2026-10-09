package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.LinkedHashSet;
import java.util.List;
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
 * Adds one or more tags to a person identified using their displayed index.
 */
public class TagCommand extends Command {

    public static final String COMMAND_WORD = "tag";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Adds one or more tags to the student identified by the displayed index.\n"
            + "Parameters: INDEX TAG [MORE_TAGS] (INDEX must be a positive integer)\n"
            + "Example: " + COMMAND_WORD + " 3 exam-prep needs-follow-up";

    public static final String MESSAGE_TAG_PERSON_SUCCESS = "Added tag(s) to %1$s: %2$s";
    public static final String MESSAGE_DUPLICATE_TAG = "One or more tags already exist for this student.";

    private final Index targetIndex;
    private final Set<Tag> tagsToAdd;

    /**
     * Creates a command that adds {@code tagsToAdd} to the person at {@code targetIndex}.
     */
    public TagCommand(Index targetIndex, Set<Tag> tagsToAdd) {
        requireNonNull(targetIndex);
        requireNonNull(tagsToAdd);
        this.targetIndex = targetIndex;
        this.tagsToAdd = new LinkedHashSet<>(tagsToAdd);
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToTag = lastShownList.get(targetIndex.getZeroBased());
        Set<Tag> updatedTags = new LinkedHashSet<>(personToTag.getTags());

        if (tagsToAdd.stream().anyMatch(updatedTags::contains)) {
            throw new CommandException(MESSAGE_DUPLICATE_TAG);
        }
        if (updatedTags.size() + tagsToAdd.size() > Tag.MAX_TAGS_PER_PERSON) {
            throw new CommandException(Tag.MESSAGE_TAG_LIMIT);
        }

        updatedTags.addAll(tagsToAdd);
        Person taggedPerson = new Person(personToTag.getName(), personToTag.getPhone(), personToTag.getEmail(),
                personToTag.getAddress(), updatedTags, personToTag.getSubjects(), personToTag.getCost(),
                personToTag.getLesson());
        model.setPerson(personToTag, taggedPerson);

        return new CommandResult(String.format(MESSAGE_TAG_PERSON_SUCCESS,
                personToTag.getName(), formatTags(tagsToAdd)));
    }

    private static String formatTags(Set<Tag> tags) {
        return tags.stream().map(tag -> tag.tagName).collect(Collectors.joining(", "));
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

        return targetIndex.equals(otherTagCommand.targetIndex)
                && tagsToAdd.equals(otherTagCommand.tagsToAdd);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("tagsToAdd", tagsToAdd)
                .toString();
    }
}
