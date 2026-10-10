package seedu.address.model.timetable;

import static java.util.Objects.requireNonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/** Builds a read-only timetable projection from the lessons owned by students. */
public final class TimetableBuilder {

    private TimetableBuilder() {
    }

    /**
     * Builds day groups from {@code today} through Saturday, including empty days.
     */
    public static List<TimetableDay> build(List<Person> persons, LocalDate today) {
        requireNonNull(persons);
        requireNonNull(today);

        List<DayOfWeek> visibleDays = getVisibleDays(today.getDayOfWeek());
        Map<Lesson, Set<Person>> studentsByLesson = new LinkedHashMap<>();

        for (Person person : persons) {
            requireNonNull(person);
            for (Lesson lesson : person.getLessons()) {
                if (visibleDays.contains(lesson.getTiming().getDay())) {
                    studentsByLesson.computeIfAbsent(lesson, unused -> new LinkedHashSet<>()).add(person);
                }
            }
        }

        Comparator<TimetableEntry> entryComparator = Comparator
                .comparing((TimetableEntry entry) -> entry.getLesson().getTiming().getStartTime())
                .thenComparing(entry -> entry.getLesson().getSubject().toString(),
                        String.CASE_INSENSITIVE_ORDER)
                .thenComparing(entry -> entry.getLesson().getTiming().getEndTime());

        List<TimetableDay> timetable = new ArrayList<>();
        for (DayOfWeek day : visibleDays) {
            List<TimetableEntry> entries = studentsByLesson.entrySet().stream()
                    .filter(entry -> entry.getKey().getTiming().getDay() == day)
                    .map(entry -> new TimetableEntry(entry.getKey(), List.copyOf(entry.getValue())))
                    .sorted(entryComparator)
                    .toList();
            timetable.add(new TimetableDay(day, entries));
        }
        return List.copyOf(timetable);
    }

    private static List<DayOfWeek> getVisibleDays(DayOfWeek currentDay) {
        List<DayOfWeek> visibleDays = new ArrayList<>();
        DayOfWeek day = currentDay;
        while (true) {
            visibleDays.add(day);
            if (day == DayOfWeek.SATURDAY) {
                return List.copyOf(visibleDays);
            }
            day = day.plus(1);
        }
    }
}
