package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A weekly lesson slot with a start and end time on the same day. */
public class LessonTiming implements Comparable<LessonTiming> {

    public static final String MESSAGE_CONSTRAINTS =
            "Lesson timing should use DAYHHmm-HHmm (for example, Monday1800-1930), with end after start.";
    private static final Pattern TIMING_PATTERN = Pattern.compile(
            "(?i)(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)"
                    + "((?:[01]\\d|2[0-3])[0-5]\\d)-((?:[01]\\d|2[0-3])[0-5]\\d)");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final DayOfWeek day;
    private final LocalTime startTime;
    private final LocalTime endTime;

    /** Creates a weekly timing from its command representation. */
    public LessonTiming(String value) {
        requireNonNull(value);
        checkArgument(isValidLessonTiming(value), MESSAGE_CONSTRAINTS);
        Matcher matcher = TIMING_PATTERN.matcher(value);
        matcher.matches();
        day = DayOfWeek.valueOf(matcher.group(1).toUpperCase(Locale.ROOT));
        startTime = LocalTime.parse(matcher.group(2), TIME_FORMAT);
        endTime = LocalTime.parse(matcher.group(3), TIME_FORMAT);
    }

    /** Returns whether the supplied value is a valid weekly lesson timing. */
    public static boolean isValidLessonTiming(String test) {
        Matcher matcher = TIMING_PATTERN.matcher(test);
        if (!matcher.matches()) {
            return false;
        }
        try {
            LocalTime start = LocalTime.parse(matcher.group(2), TIME_FORMAT);
            LocalTime end = LocalTime.parse(matcher.group(3), TIME_FORMAT);
            return end.isAfter(start);
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    public DayOfWeek getDay() {
        return day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public boolean startsAt(DayOfWeek expectedDay, LocalTime expectedTime) {
        return day == expectedDay && startTime.equals(expectedTime);
    }

    @Override
    public int compareTo(LessonTiming other) {
        int dayComparison = Integer.compare(day.getValue(), other.day.getValue());
        return dayComparison != 0 ? dayComparison : startTime.compareTo(other.startTime);
    }

    @Override
    public String toString() {
        String displayDay = day.toString().substring(0, 1)
                + day.toString().substring(1).toLowerCase(Locale.ROOT);
        return displayDay + " " + startTime.format(DISPLAY_TIME_FORMAT) + "-" + endTime.format(DISPLAY_TIME_FORMAT);
    }

    /** Returns the compact form used in JSON and commands. */
    public String toStorageString() {
        String displayDay = day.toString().substring(0, 1)
                + day.toString().substring(1).toLowerCase(Locale.ROOT);
        return displayDay + startTime.format(TIME_FORMAT) + "-" + endTime.format(TIME_FORMAT);
    }

    @Override
    public boolean equals(Object other) {
        if (other == this) {
            return true;
        }
        if (!(other instanceof LessonTiming otherTiming)) {
            return false;
        }
        return day == otherTiming.day && startTime.equals(otherTiming.startTime)
                && endTime.equals(otherTiming.endTime);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(day, startTime, endTime);
    }
}
