package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

import seedu.address.model.lesson.Cost;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonTiming;
import seedu.address.model.lesson.Subject;
import seedu.address.model.person.Person;
import seedu.address.model.timetable.TimetableEntry;
import seedu.address.testutil.PersonBuilder;

public class TimetableEntryCardTest {

    @Test
    public void formatters_validEntry_returnDisplayText() {
        Lesson lesson = new Lesson(new Subject("Math"), new Cost("30"),
                new LessonTiming("Monday1800-1930"));
        Person zoe = new PersonBuilder().withName("Zoe Tan").build();
        Person alice = new PersonBuilder().withName("Alice Tan").withEmail("alice@example.com")
                .withPhone("91234567").build();
        TimetableEntry entry = new TimetableEntry(lesson, List.of(zoe, alice));

        assertEquals("18:00-19:30", TimetableEntryCard.formatTimeRange(entry));
        assertEquals("Alice Tan, Zoe Tan", TimetableEntryCard.formatStudents(entry));
    }
}
