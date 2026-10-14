package seedu.address.model.person;

import java.util.List;
import java.util.Locale;
import java.util.function.Predicate;
import java.util.stream.Stream;

import seedu.address.commons.util.ToStringBuilder;

/** Matches any subject keyword against a student's current subjects, ignoring case. */
public class SubjectContainsKeywordsPredicate implements Predicate<Person> {

    private final List<String> keywords;

    public SubjectContainsKeywordsPredicate(List<String> keywords) {
        this.keywords = List.copyOf(keywords);
    }

    @Override
    public boolean test(Person person) {
        Stream<String> subjects = person.getLessons().stream().map(lesson -> lesson.getSubject().toString());
        // Complete legacy records have already been migrated. Their retained metadata may be stale after unenrolment.
        if (person.getLessons().isEmpty() && (person.getCost().isEmpty() || person.getLesson().isEmpty())) {
            subjects = person.getSubjects().stream().map(Subject::toString);
        }
        return subjects.map(subject -> subject.toLowerCase(Locale.ROOT))
                .anyMatch(subject -> keywords.stream().filter(keyword -> !keyword.isBlank())
                        .anyMatch(keyword -> subject.contains(keyword.toLowerCase(Locale.ROOT))));
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof SubjectContainsKeywordsPredicate otherPredicate
                && keywords.equals(otherPredicate.keywords);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this).add("keywords", keywords).toString();
    }
}
