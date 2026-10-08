package seedu.address.model.timetable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;
import seedu.address.testutil.PersonBuilder;

public class TimetableBuilderTest {

    private static final LocalDate SUNDAY = LocalDate.of(2026, 10, 4);
    private static final LocalDate THURSDAY = LocalDate.of(2026, 10, 8);
    private static final LocalDate SATURDAY = LocalDate.of(2026, 10, 10);

    @Test
    public void build_createsTodayThroughSaturdayIncludingEmptyDays() {
        List<TimetableDay> fromSunday = TimetableBuilder.build(List.of(), SUNDAY);
        List<TimetableDay> fromThursday = TimetableBuilder.build(List.of(), THURSDAY);
        List<TimetableDay> fromSaturday = TimetableBuilder.build(List.of(), SATURDAY);

        assertEquals(7, fromSunday.size());
        assertEquals(List.of(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY),
                fromThursday.stream().map(TimetableDay::getDay).toList());
        assertEquals(List.of(DayOfWeek.SATURDAY),
                fromSaturday.stream().map(TimetableDay::getDay).toList());
        assertTrue(fromThursday.stream().allMatch(day -> day.getEntries().isEmpty()));
    }

    @Test
    public void build_excludesEarlierDaysButIncludesAllOfToday() {
        Lesson wednesdayLesson = lesson("Math", "30", "Wednesday1800-1930");
        Lesson earlyThursdayLesson = lesson("English", "25", "Thursday0800-0900");
        Lesson saturdayLesson = lesson("Physics", "35", "Saturday1500-1630");
        Person student = new PersonBuilder().withLessons(
                wednesdayLesson, earlyThursdayLesson, saturdayLesson).build();

        List<TimetableDay> timetable = TimetableBuilder.build(List.of(student), THURSDAY);

        assertEquals(List.of(earlyThursdayLesson),
                timetable.get(0).getEntries().stream().map(TimetableEntry::getLesson).toList());
        assertEquals(List.of(saturdayLesson),
                timetable.get(2).getEntries().stream().map(TimetableEntry::getLesson).toList());
    }

    @Test
    public void build_groupsEqualLessonsAndSortsStudents() {
        Lesson sharedLesson = lesson("Math", "30", "Friday1800-1930");
        Person zoe = new PersonBuilder().withName("Zoe Tan").withLessons(sharedLesson).build();
        Person alice = new PersonBuilder().withName("Alice Tan").withEmail("alice@example.com")
                .withPhone("91234567").withLessons(sharedLesson).build();

        TimetableEntry entry = TimetableBuilder.build(List.of(zoe, alice, alice), THURSDAY)
                .get(1).getEntries().get(0);

        assertEquals(sharedLesson, entry.getLesson());
        assertEquals(List.of("Alice Tan", "Zoe Tan"), entry.getStudents().stream()
                .map(person -> person.getName().fullName).toList());
    }

    @Test
    public void build_sortsEntriesByStartSubjectThenEnd() {
        Lesson later = lesson("Biology", "30", "Friday1800-1900");
        Lesson mathLong = lesson("Math", "30", "Friday1600-1800");
        Lesson english = lesson("English", "30", "Friday1600-1700");
        Lesson mathShort = lesson("Math", "30", "Friday1600-1700");
        Person student = new PersonBuilder().withLessons(later, mathLong, english, mathShort).build();

        List<Lesson> orderedLessons = TimetableBuilder.build(List.of(student), THURSDAY)
                .get(1).getEntries().stream().map(TimetableEntry::getLesson).toList();

        assertEquals(List.of(english, mathShort, mathLong, later), orderedLessons);
    }

    @Test
    public void build_supportsMultipleOrNoLessonsAndReturnsImmutableLists() {
        Lesson fridayLesson = lesson("Math", "30", "Friday1800-1930");
        Lesson saturdayLesson = lesson("Physics", "35", "Saturday1000-1130");
        Person enrolled = new PersonBuilder().withLessons(fridayLesson, saturdayLesson).build();
        Person notEnrolled = new PersonBuilder().withName("No Lessons").withEmail("none@example.com")
                .withPhone("87654321").build();

        List<TimetableDay> timetable = TimetableBuilder.build(List.of(enrolled, notEnrolled), THURSDAY);

        assertEquals(2, timetable.stream().mapToInt(day -> day.getEntries().size()).sum());
        assertThrows(UnsupportedOperationException.class, () -> timetable.add(timetable.get(0)));
        assertThrows(UnsupportedOperationException.class, () ->
                timetable.get(0).getEntries().add(timetable.get(1).getEntries().get(0)));
        assertThrows(UnsupportedOperationException.class, () ->
                timetable.get(1).getEntries().get(0).getStudents().add(notEnrolled));
    }

    private static Lesson lesson(String subject, String cost, String timing) {
        return new Lesson(new Subject(subject), new Cost(cost), new LessonTiming(timing));
    }
}
