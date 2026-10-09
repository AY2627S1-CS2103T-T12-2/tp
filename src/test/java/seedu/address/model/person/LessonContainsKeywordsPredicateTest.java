package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.testutil.PersonBuilder;

public class LessonContainsKeywordsPredicateTest {

    @Test
    public void equals() {
        List<String> firstPredicateKeywordList = List.of("first");
        List<String> secondPredicateKeywordList = List.of("first", "second");

        LessonContainsKeywordsPredicate firstPredicate =
                new LessonContainsKeywordsPredicate(firstPredicateKeywordList);
        LessonContainsKeywordsPredicate secondPredicate =
                new LessonContainsKeywordsPredicate(secondPredicateKeywordList);

        // same object -> returns true
        assertTrue(firstPredicate.equals(firstPredicate));

        // same values -> returns true
        LessonContainsKeywordsPredicate firstPredicateCopy =
                new LessonContainsKeywordsPredicate(firstPredicateKeywordList);
        assertTrue(firstPredicate.equals(firstPredicateCopy));

        // different types -> returns false
        assertFalse(firstPredicate.equals(1));

        // null -> returns false
        assertFalse(firstPredicate.equals(null));

        // different person -> returns false
        assertFalse(firstPredicate.equals(secondPredicate));
    }

    @Test
    public void test_lessonContainsKeywords_returnsTrue() {
        // Exact keyword
        LessonContainsKeywordsPredicate predicate = new LessonContainsKeywordsPredicate(List.of("Monday1800"));
        assertTrue(predicate.test(personWithLesson("Monday1800")));

        // Partial day keyword
        predicate = new LessonContainsKeywordsPredicate(List.of("Monday"));
        assertTrue(predicate.test(personWithLesson("Monday1800")));

        // Partial time keyword
        predicate = new LessonContainsKeywordsPredicate(List.of("1800"));
        assertTrue(predicate.test(personWithLesson("Monday1800")));

        // Only one matching keyword
        predicate = new LessonContainsKeywordsPredicate(List.of("Tuesday", "1800"));
        assertTrue(predicate.test(personWithLesson("Monday1800")));

        // Mixed-case keywords
        predicate = new LessonContainsKeywordsPredicate(List.of("mOnDaY"));
        assertTrue(predicate.test(personWithLesson("Monday1800")));
    }

    @Test
    public void test_lessonDoesNotContainKeywords_returnsFalse() {
        // Zero keywords
        LessonContainsKeywordsPredicate predicate = new LessonContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(personWithLesson("Monday1800")));

        // Non-matching keyword
        predicate = new LessonContainsKeywordsPredicate(List.of("Tuesday"));
        assertFalse(predicate.test(personWithLesson("Monday1800")));

        // Keywords match name, phone, email and address, but do not match lesson
        predicate = new LessonContainsKeywordsPredicate(List.of("Alice", "12345", "alice@email.com", "Main"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").withAddress("Main Street").build()));

        // Person has no lesson
        predicate = new LessonContainsKeywordsPredicate(List.of("Monday"));
        assertFalse(predicate.test(new PersonBuilder().build()));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("keyword1", "keyword2");
        LessonContainsKeywordsPredicate predicate = new LessonContainsKeywordsPredicate(keywords);

        String expected = LessonContainsKeywordsPredicate.class.getCanonicalName() + "{keywords=" + keywords + "}";
        assertEquals(expected, predicate.toString());
    }

    private Person personWithLesson(String lesson) {
        Person person = new PersonBuilder().build();
        return new Person(person.getName(), person.getPhone(), person.getEmail(), person.getAddress(),
                person.getTags(), Set.of(), Optional.empty(), Optional.of(new Lesson(lesson)));
    }
}
