package seedu.address.model.person;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.OneOffLessonSchedule;
import seedu.address.model.lesson.Subject;
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

        // same day but different time -> returns false
        LessonContainsKeywordsPredicate sameDayDifferentTimePredicate =
                new LessonContainsKeywordsPredicate(Optional.of("first"), Optional.of("1800"));
        LessonContainsKeywordsPredicate sameDayDifferentTimePredicateCopy =
                new LessonContainsKeywordsPredicate(Optional.of("first"), Optional.of("1900"));
        assertFalse(sameDayDifferentTimePredicate.equals(sameDayDifferentTimePredicateCopy));

        LessonContainsKeywordsPredicate sameDayAndTimeDifferentDatePredicate =
                new LessonContainsKeywordsPredicate(Optional.of("tue"), Optional.of("1800"),
                        Optional.of(LocalDate.parse("2026-10-20")));
        LessonContainsKeywordsPredicate sameDayAndTimeDifferentDatePredicateCopy =
                new LessonContainsKeywordsPredicate(Optional.of("tue"), Optional.of("1800"),
                        Optional.of(LocalDate.parse("2026-10-27")));
        assertFalse(sameDayAndTimeDifferentDatePredicate.equals(sameDayAndTimeDifferentDatePredicateCopy));
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

        // Day and time keywords both match
        predicate = new LessonContainsKeywordsPredicate(List.of("Monday", "1800"));
        assertTrue(predicate.test(personWithLesson("Monday1800")));

        // Mixed-case keywords
        predicate = new LessonContainsKeywordsPredicate(List.of("mOnDaY"));
        assertTrue(predicate.test(personWithLesson("Monday1800")));

        // Structured class day and start time match
        predicate = new LessonContainsKeywordsPredicate(List.of("Mon", "1800"));
        assertTrue(predicate.test(personWithClass("Math", "30", "Monday1800-1930")));

        // Structured class end time matches
        predicate = new LessonContainsKeywordsPredicate(List.of("1930"));
        assertTrue(predicate.test(personWithClass("Math", "30", "Monday1800-1930")));

        // One-off classes match their exact date, weekday, and time
        predicate = new LessonContainsKeywordsPredicate(List.of("2026-10-20"));
        assertTrue(predicate.test(personWithOneOffClass("2026-10-20T1800-1930")));
        predicate = new LessonContainsKeywordsPredicate(List.of("Tue", "1800"));
        assertTrue(predicate.test(personWithOneOffClass("2026-10-20T1800-1930")));
    }

    @Test
    public void test_lessonDoesNotContainKeywords_returnsFalse() {
        // Zero keywords
        LessonContainsKeywordsPredicate predicate = new LessonContainsKeywordsPredicate(List.of());
        assertFalse(predicate.test(personWithLesson("Monday1800")));

        // Non-matching keyword
        predicate = new LessonContainsKeywordsPredicate(List.of("Tuesday"));
        assertFalse(predicate.test(personWithLesson("Monday1800")));

        // Time matches but day does not match
        predicate = new LessonContainsKeywordsPredicate(List.of("Tuesday", "1800"));
        assertFalse(predicate.test(personWithLesson("Monday1800")));

        // Keywords match name, phone, email and address, but do not match lesson
        predicate = new LessonContainsKeywordsPredicate(List.of("Alice", "12345", "alice@email.com", "Main"));
        assertFalse(predicate.test(new PersonBuilder().withName("Alice").withPhone("12345")
                .withEmail("alice@email.com").withAddress("Main Street").build()));

        // Person has no lesson
        predicate = new LessonContainsKeywordsPredicate(List.of("Monday"));
        assertFalse(predicate.test(new PersonBuilder().build()));

        // Structured class time matches but day does not match
        predicate = new LessonContainsKeywordsPredicate(List.of("Tuesday", "1800"));
        assertFalse(predicate.test(personWithClass("Math", "30", "Monday1800-1930")));

        // Structured class day matches but time does not match
        predicate = new LessonContainsKeywordsPredicate(List.of("Monday", "2359"));
        assertFalse(predicate.test(personWithClass("Math", "30", "Monday1800-1930")));

        predicate = new LessonContainsKeywordsPredicate(List.of("2026-10-21"));
        assertFalse(predicate.test(personWithOneOffClass("2026-10-20T1800-1930")));
    }

    @Test
    public void score_multipleMatchingClasses_countsEachSearchComponentOnce() {
        Person person = new PersonBuilder().withLessons(
                new seedu.address.model.lesson.Lesson(new Subject("Math"), new Cost("30"),
                        new LessonTiming("Monday1800-1900")),
                new seedu.address.model.lesson.Lesson(new Subject("Physics"), new Cost("40"),
                        new LessonTiming("Monday1900-2000"))).build();

        assertEquals(1, new LessonContainsKeywordsPredicate(List.of("Mon")).score(person));
        assertEquals(2, new LessonContainsKeywordsPredicate(List.of("Mon", "1900")).score(person));
    }

    @Test
    public void score_multipleOneOffClassesOnSameDate_countsDateOnce() {
        Person person = new PersonBuilder().withLessons(
                new seedu.address.model.lesson.Lesson(new Subject("Math"), new Cost("30"),
                        new OneOffLessonSchedule("2026-10-20T1800-1900")),
                new seedu.address.model.lesson.Lesson(new Subject("Physics"), new Cost("40"),
                        new OneOffLessonSchedule("2026-10-20T1900-2000"))).build();

        assertEquals(1, new LessonContainsKeywordsPredicate(List.of("2026-10-20")).score(person));
    }

    @Test
    public void score_keywordsMatchingDifferentClasses_doesNotMatch() {
        Person person = new PersonBuilder().withLessons(
                new seedu.address.model.lesson.Lesson(new Subject("Math"), new Cost("30"),
                        new LessonTiming("Monday1800-1830")),
                new seedu.address.model.lesson.Lesson(new Subject("Physics"), new Cost("40"),
                        new LessonTiming("Tuesday1900-1930"))).build();

        assertEquals(0, new LessonContainsKeywordsPredicate(List.of("Mon", "1900")).score(person));
    }

    @Test
    public void toStringMethod() {
        List<String> keywords = List.of("Monday", "1800");
        LessonContainsKeywordsPredicate predicate = new LessonContainsKeywordsPredicate(keywords);

        String expected = LessonContainsKeywordsPredicate.class.getCanonicalName()
                + "{dayKeyword=Optional[monday], timeKeyword=Optional[1800], dateKeyword=Optional.empty}";
        assertEquals(expected, predicate.toString());
    }

    private Person personWithLesson(String lesson) {
        Person person = new PersonBuilder().build();
        return new Person(person.getName(), person.getPhone(), person.getEmail(), person.getAddress(),
                person.getTags(), Set.of(), Optional.empty(), Optional.of(new Lesson(lesson)));
    }

    private Person personWithClass(String subject, String cost, String timing) {
        Person person = new PersonBuilder().build();
        seedu.address.model.lesson.Lesson lesson = new seedu.address.model.lesson.Lesson(
                new seedu.address.model.lesson.Subject(subject),
                new seedu.address.model.lesson.Cost(cost),
                new seedu.address.model.lesson.LessonTiming(timing));
        return new Person(person.getName(), person.getPhone(), person.getEmail(), person.getAddress(),
                person.getTags(), Set.<seedu.address.model.lesson.Lesson>of(lesson));
    }

    private Person personWithOneOffClass(String timing) {
        Person person = new PersonBuilder().build();
        seedu.address.model.lesson.Lesson lesson = new seedu.address.model.lesson.Lesson(
                new seedu.address.model.lesson.Subject("Math"),
                new seedu.address.model.lesson.Cost("30"),
                new seedu.address.model.lesson.OneOffLessonSchedule(timing));
        return new Person(person.getName(), person.getPhone(), person.getEmail(), person.getAddress(),
                person.getTags(), Set.of(lesson));
    }
}
