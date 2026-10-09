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
 * Removes one or more tags from a person identified using their displayed index.
 */
public class UntagCommand extends Command {

    public static final String COMMAND_WORD = "untag";
    public static final String REMOVE_ALL_FLAG = "-all";

    public static final String MESSAGE_USAGE = COMMAND_WORD
            + ": Removes one or more tags from the student identified by the displayed index.\n"
            + "Parameters: untag <index> <tag> / untag <index> -all\n"
            + "Example: " + COMMAND_WORD + " 2 parent-follow-up";

    public static final String MESSAGE_UNTAG_PERSON_SUCCESS = "Removed tag(s) from %1$s: %2$s";
    public static final String MESSAGE_UNTAG_ALL_SUCCESS = "Removed all tags from %1$s.";
    public static final String MESSAGE_TAG_NOT_FOUND = "One or more tags do not exist for this student.";

    private final Index targetIndex;
    private final Set<Tag> tagsToRemove;
    private final boolean shouldRemoveAll;

    /**
     * Creates a command that removes {@code tagsToRemove} from the person at {@code targetIndex}.
     */
    public UntagCommand(Index targetIndex, Set<Tag> tagsToRemove) {
        requireNonNull(targetIndex);
        requireNonNull(tagsToRemove);
        this.targetIndex = targetIndex;
        this.tagsToRemove = new LinkedHashSet<>(tagsToRemove);
        shouldRemoveAll = false;
    }

    /**
     * Creates a command that removes every tag from the person at {@code targetIndex}.
     */
    public UntagCommand(Index targetIndex) {
        requireNonNull(targetIndex);
        this.targetIndex = targetIndex;
        tagsToRemove = Set.of();
        shouldRemoveAll = true;
    }

    @Override
    public CommandResult execute(Model model) throws CommandException {
        requireNonNull(model);
        List<Person> lastShownList = model.getFilteredPersonList();

        if (targetIndex.getZeroBased() >= lastShownList.size()) {
            throw new CommandException(Messages.MESSAGE_INVALID_PERSON_DISPLAYED_INDEX);
        }

        Person personToUntag = lastShownList.get(targetIndex.getZeroBased());
        Set<Tag> updatedTags = new LinkedHashSet<>(personToUntag.getTags());

        if (!shouldRemoveAll && !updatedTags.containsAll(tagsToRemove)) {
            throw new CommandException(MESSAGE_TAG_NOT_FOUND);
        }

        if (shouldRemoveAll) {
            updatedTags.clear();
        } else {
            updatedTags.removeAll(tagsToRemove);
        }

        Person untaggedPerson = new Person(personToUntag.getName(), personToUntag.getPhone(), personToUntag.getEmail(),
                personToUntag.getAddress(), updatedTags, personToUntag.getSubjects(), personToUntag.getCost(),
                personToUntag.getLesson());
        model.setPerson(personToUntag, untaggedPerson);

        if (shouldRemoveAll) {
            return new CommandResult(String.format(MESSAGE_UNTAG_ALL_SUCCESS, personToUntag.getName()));
        }
        return new CommandResult(String.format(MESSAGE_UNTAG_PERSON_SUCCESS, personToUntag.getName(),
                formatTags(tagsToRemove)));
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
        if (!(other instanceof UntagCommand otherUntagCommand)) {
            return false;
        }

        return targetIndex.equals(otherUntagCommand.targetIndex)
                && tagsToRemove.equals(otherUntagCommand.tagsToRemove)
                && shouldRemoveAll == otherUntagCommand.shouldRemoveAll;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .add("targetIndex", targetIndex)
                .add("tagsToRemove", tagsToRemove)
                .add("shouldRemoveAll", shouldRemoveAll)
                .toString();
    }
}
