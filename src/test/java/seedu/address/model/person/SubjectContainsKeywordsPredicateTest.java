package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.testutil.PersonBuilder;

public class SubjectContainsKeywordsPredicateTest {

    @Test
    public void test_matchesAnyKeywordIgnoringCaseAndUsingSubstrings() {
        Person student = new PersonBuilder().withLessons(lesson("Additional Mathematics"), lesson("Physics")).build();
        assertTrue(predicate("mAtH").test(student));
        assertTrue(predicate("English", "phys").test(student));
        assertTrue(predicate("additional", "mathematics").test(student));
        assertFalse(predicate("English", "Chemistry").test(student));
        assertFalse(predicate().test(student));
        assertFalse(predicate("", " ").test(student));
    }

    @Test
    public void test_doesNotSearchOtherFields() {
        Person student = new PersonBuilder().withName("Math Student").withAddress("Math Street")
                .withEmail("math@example.com").withTags("math").build();
        assertFalse(predicate("math").test(student));
    }

    @Test
    public void test_supportsLegacySubjectOnlyAndMigratedRecords() {
        Person base = new PersonBuilder().build();
        Set<seedu.address.model.person.Subject> subjects = Set.of(new seedu.address.model.person.Subject("Math"));
        Person subjectOnly = new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getTags(), subjects, Optional.empty(), Optional.empty());
        Person migrated = new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getTags(), subjects, Optional.of(new seedu.address.model.person.Cost("30")),
                Optional.of(new seedu.address.model.person.Lesson("Monday1800")));
        assertTrue(predicate("math").test(subjectOnly));
        assertTrue(predicate("math").test(migrated));

        // Simulate removing a migrated class while retaining its legacy metadata, as PR #62 does.
        Person unenrolled = new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getTags(), Set.of(), migrated.getSubjects(), migrated.getCost(), migrated.getLesson());
        assertFalse(predicate("math").test(unenrolled));
        Person changedClasses = new Person(base.getName(), base.getPhone(), base.getEmail(), base.getAddress(),
                base.getTags(), Set.of(lesson("Physics")), migrated.getSubjects(), migrated.getCost(),
                migrated.getLesson());
        assertFalse(predicate("math").test(changedClasses));
        assertTrue(predicate("physics").test(changedClasses));
    }

    @Test
    public void equalsAndToString_andKeywordsAreCopied() {
        List<String> keywords = new ArrayList<>(List.of("Math"));
        SubjectContainsKeywordsPredicate predicate = new SubjectContainsKeywordsPredicate(keywords);
        keywords.clear();
        assertEquals(predicate, predicate);
        assertEquals(predicate("Math"), predicate);
        assertNotEquals(predicate("Physics"), predicate);
        assertNotEquals(null, predicate);
        assertNotEquals("Math", predicate);
        assertEquals(SubjectContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=[Math]}",
                predicate.toString());
    }

    private SubjectContainsKeywordsPredicate predicate(String... keywords) {
        return new SubjectContainsKeywordsPredicate(List.of(keywords));
    }

    private Lesson lesson(String subject) {
        return new Lesson(new Subject(subject), new Cost("30"), new LessonTiming("Monday1800-1930"));
    }
}
