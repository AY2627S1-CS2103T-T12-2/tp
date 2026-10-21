package seedu.address.model.timetable;

import static java.util.Objects.requireNonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import seedu.address.model.lesson.Enrollment;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.lesson.LessonSchedule;
import seedu.address.model.lesson.Subject;
import seedu.address.model.lesson.TuitionClass;
import seedu.address.model.person.Person;

/** Builds a read-only timetable projection from the lessons owned by students. */
public final class TimetableBuilder {

    private TimetableBuilder() {
    }

    /**
     * Builds day groups from {@code today} through Saturday, including empty days.
     */
    public static List<TimetableDay> build(List<Person> persons, LocalDate today) {
        return build(List.of(), persons, today);
    }

    /** Builds a timetable that also includes independently stored classes without students. */
    public static List<TimetableDay> build(List<TuitionClass> tuitionClasses, List<Person> persons, LocalDate today) {
        requireNonNull(tuitionClasses);
        requireNonNull(persons);
        requireNonNull(today);

        List<LocalDate> visibleDates = getVisibleDates(today);
        Map<ClassIdentity, ClassGroup> groupsByClass = new LinkedHashMap<>();

        for (TuitionClass tuitionClass : tuitionClasses) {
            if (visibleDates.stream().anyMatch(tuitionClass.getSchedule()::occursOn)) {
                Lesson lesson = new Enrollment(tuitionClass);
                groupsByClass.put(new ClassIdentity(lesson.getSubject(), lesson.getSchedule()), new ClassGroup(lesson));
            }
        }

        for (Person person : persons) {
            requireNonNull(person);
            for (Lesson lesson : person.getLessons()) {
                if (visibleDates.stream().anyMatch(lesson.getSchedule()::occursOn)) {
                    ClassIdentity identity = new ClassIdentity(lesson.getSubject(), lesson.getSchedule());
                    groupsByClass.computeIfAbsent(identity, unused -> new ClassGroup(lesson)).students.add(person);
                }
            }
        }

        Comparator<TimetableEntry> entryComparator = Comparator
                .comparing((TimetableEntry entry) -> entry.getLesson().getSchedule().getStartTime())
                .thenComparing(entry -> entry.getLesson().getSubject().toString(),
                        String.CASE_INSENSITIVE_ORDER)
                .thenComparing(entry -> entry.getLesson().getSchedule().getEndTime());

        List<TimetableDay> timetable = new ArrayList<>();
        for (LocalDate date : visibleDates) {
            List<TimetableEntry> entries = groupsByClass.values().stream()
                    .filter(group -> group.lesson.getSchedule().occursOn(date))
                    .map(group -> new TimetableEntry(group.lesson, List.copyOf(group.students)))
                    .sorted(entryComparator)
                    .toList();
            timetable.add(new TimetableDay(date.getDayOfWeek(), entries));
        }
        return List.copyOf(timetable);
    }

    private static List<LocalDate> getVisibleDates(LocalDate today) {
        List<LocalDate> visibleDates = new ArrayList<>();
        LocalDate date = today;
        while (true) {
            visibleDates.add(date);
            if (date.getDayOfWeek() == java.time.DayOfWeek.SATURDAY) {
                return List.copyOf(visibleDates);
            }
            date = date.plusDays(1);
        }
    }

    /** Fields that identify a logical class regardless of an enrolment's payment status. */
    private record ClassIdentity(Subject subject, LessonSchedule schedule) {
    }

    /** Accumulates the students belonging to one logical class. */
    private static class ClassGroup {
        private final Lesson lesson;
        private final Set<Person> students = new LinkedHashSet<>();

        ClassGroup(Lesson enrolment) {
            lesson = new Lesson(enrolment.getClassId(), enrolment.getSubject(), enrolment.getCost(),
                    enrolment.getSchedule(), seedu.address.model.lesson.PaymentStatus.UNPAID);
        }
    }
}
