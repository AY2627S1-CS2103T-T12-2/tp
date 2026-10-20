package seedu.address.model.person;

import java.util.List;

import seedu.address.commons.util.StringUtil;
import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Tag} matches any of the keywords given.
 */
public class TagContainsKeywordsPredicate implements ScoredPersonPredicate {
    private final List<String> keywords;

    /**
     * Creates a predicate that matches tags containing any of the keywords.
     */
    public TagContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords.stream()
                .filter(keyword -> !keyword.isBlank())
                .toList();
    }

    @Override
    public boolean test(Person person) {
        return score(person) > 0;
    }

    @Override
    public int score(Person person) {
        return (int) keywords.stream()
                .filter(keyword -> person.getTags().stream()
                        .anyMatch(tag -> StringUtil.containsIgnoreCase(tag.tagName, keyword)))
                .count();
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof TagContainsKeywordsPredicate otherTagContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherTagContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
