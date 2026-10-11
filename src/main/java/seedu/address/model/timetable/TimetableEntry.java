package seedu.address.model.timetable;

import static java.util.Objects.requireNonNull;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/** An immutable class entry displayed in the timetable. */
public class TimetableEntry {

    private final Lesson lesson;
    private final List<Person> students;

    /** Creates an entry for one lesson and its attending students. */
    public TimetableEntry(Lesson lesson, List<Person> students) {
        this.lesson = requireNonNull(lesson);
        requireNonNull(students);
        this.students = students.stream()
                .distinct()
                .sorted(Comparator.comparing(person -> person.getName().fullName,
                        String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    public Lesson getLesson() {
        return lesson;
    }

    public List<Person> getStudents() {
        return students;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof TimetableEntry otherEntry
                && lesson.equals(otherEntry.lesson) && students.equals(otherEntry.students);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lesson, students);
    }
}
