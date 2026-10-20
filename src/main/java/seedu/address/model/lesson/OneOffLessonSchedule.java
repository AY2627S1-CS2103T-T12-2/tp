package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A lesson schedule that occurs once on an exact date. */
public final class OneOffLessonSchedule implements LessonSchedule, Comparable<OneOffLessonSchedule> {

    public static final String MESSAGE_CONSTRAINTS =
            "One-off class timing should use yyyy-MM-ddTHHmm-HHmm "
                    + "(for example, 2026-10-20T1800-1930), with end after start.";
    private static final Pattern TIMING_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2})T((?:[01]\\d|2[0-3])[0-5]\\d)-"
                    + "((?:[01]\\d|2[0-3])[0-5]\\d)");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;

    /** Creates a one-off schedule from its command representation. */
    public OneOffLessonSchedule(String value) {
        requireNonNull(value);
        checkArgument(isValidOneOffLessonSchedule(value), MESSAGE_CONSTRAINTS);
        Matcher matcher = TIMING_PATTERN.matcher(value);
        matcher.matches();
        date = LocalDate.parse(matcher.group(1));
        startTime = LocalTime.parse(matcher.group(2), TIME_FORMAT);
        endTime = LocalTime.parse(matcher.group(3), TIME_FORMAT);
    }

    /** Returns whether {@code test} is a valid one-off schedule representation. */
    public static boolean isValidOneOffLessonSchedule(String test) {
        if (test == null) {
            return false;
        }
        Matcher matcher = TIMING_PATTERN.matcher(test);
        if (!matcher.matches()) {
            return false;
        }
        try {
            LocalDate.parse(matcher.group(1));
            LocalTime start = LocalTime.parse(matcher.group(2), TIME_FORMAT);
            LocalTime end = LocalTime.parse(matcher.group(3), TIME_FORMAT);
            return end.isAfter(start);
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    @Override
    public ScheduleType getType() {
        return ScheduleType.ONE_OFF;
    }

    @Override
    public DayOfWeek getDay() {
        return date.getDayOfWeek();
    }

    @Override
    public LocalTime getStartTime() {
        return startTime;
    }

    @Override
    public LocalTime getEndTime() {
        return endTime;
    }

    @Override
    public boolean occursOn(LocalDate candidate) {
        requireNonNull(candidate);
        return date.equals(candidate);
    }

    @Override
    public Optional<LocalDate> getDate() {
        return Optional.of(date);
    }

    @Override
    public String toStorageString() {
        return date + "T" + startTime.format(TIME_FORMAT) + "-" + endTime.format(TIME_FORMAT);
    }

    @Override
    public int compareTo(OneOffLessonSchedule other) {
        int dateComparison = date.compareTo(other.date);
        return dateComparison != 0 ? dateComparison : startTime.compareTo(other.startTime);
    }

    @Override
    public String toString() {
        return date.format(DISPLAY_DATE_FORMAT) + " " + startTime.format(DISPLAY_TIME_FORMAT)
                + "-" + endTime.format(DISPLAY_TIME_FORMAT);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof OneOffLessonSchedule otherSchedule
                && date.equals(otherSchedule.date)
                && startTime.equals(otherSchedule.startTime)
                && endTime.equals(otherSchedule.endTime);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(date, startTime, endTime);
    }
}
