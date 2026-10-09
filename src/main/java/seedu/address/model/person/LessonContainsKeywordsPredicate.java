package seedu.address.model.person;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;

import seedu.address.commons.util.ToStringBuilder;

/**
 * Tests that a {@code Person}'s {@code Lesson} contains any of the keywords given.
 */
public class LessonContainsKeywordsPredicate implements Predicate<Person> {
    private final List<String> keywords;

    public LessonContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public boolean test(Person person) {
        return person.getLesson()
                .map(lesson -> containsAnyKeywordIgnoreCase(lesson.toString()))
                .orElse(false);
    }

    private boolean containsAnyKeywordIgnoreCase(String lesson) {
        String lessonLowerCase = lesson.toLowerCase(Locale.ROOT);
        return keywords.stream()
                .map(keyword -> keyword.toLowerCase(Locale.ROOT))
                .anyMatch(lessonLowerCase::contains);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }

        // instanceof handles nulls
        if (!(other instanceof LessonContainsKeywordsPredicate otherLessonContainsKeywordsPredicate)) {
            return false;
        }

        return keywords.equals(otherLessonContainsKeywordsPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
