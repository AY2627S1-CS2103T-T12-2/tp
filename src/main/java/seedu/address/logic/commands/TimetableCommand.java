package seedu.address.logic.commands;

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

import seedu.address.model.Model;
import seedu.address.model.lesson.Lesson;
import seedu.address.model.person.Person;

/** Displays recurring lessons grouped by day and ordered by start time. */
public class TimetableCommand extends Command {

    public static final String COMMAND_WORD = "timetable";
    public static final String MESSAGE_EMPTY = "No lessons scheduled.";

    @Override
    public CommandResult execute(Model model) {
        requireNonNull(model);
        Map<Lesson, java.util.List<Person>> studentsByLesson = new LinkedHashMap<>();
        for (Person person : model.getAddressBook().getPersonList()) {
            for (Lesson lesson : person.getLessons()) {
                studentsByLesson.computeIfAbsent(lesson, unused -> new ArrayList<>()).add(person);
            }
        }
        if (studentsByLesson.isEmpty()) {
            return new CommandResult(MESSAGE_EMPTY);
        }
        java.util.List<Lesson> lessons = studentsByLesson.keySet().stream()
                .sorted(Comparator.comparing(Lesson::getTiming))
                .toList();
        StringBuilder output = new StringBuilder("Weekly timetable");
        java.time.DayOfWeek previousDay = null;
        for (Lesson lesson : lessons) {
            if (lesson.getTiming().getDay() != previousDay) {
                previousDay = lesson.getTiming().getDay();
                output.append(System.lineSeparator()).append(System.lineSeparator())
                        .append(formatDay(previousDay)).append(':');
            }
            String students = studentsByLesson.get(lesson).stream()
                    .map(person -> person.getName().toString())
                    .sorted()
                    .collect(Collectors.joining(", "));
            output.append(System.lineSeparator()).append(lesson.getTiming().getStartTime()).append('-')
                    .append(lesson.getTiming().getEndTime()).append("  ")
                    .append(lesson.getSubject()).append(" — ")
                    .append(students.isEmpty() ? "No students" : students);
        }
        return new CommandResult(output.toString());
    }

    private static String formatDay(java.time.DayOfWeek day) {
        String lowerCaseDay = day.toString().toLowerCase(java.util.Locale.ROOT);
        return Character.toUpperCase(lowerCaseDay.charAt(0)) + lowerCaseDay.substring(1);
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TimetableCommand;
    }

    @Override
    public int hashCode() {
        return TimetableCommand.class.hashCode();
    }
}
