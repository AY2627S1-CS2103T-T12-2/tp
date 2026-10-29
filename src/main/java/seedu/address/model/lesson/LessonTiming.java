package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A weekly lesson slot with a start and end time on the same day. */
public class LessonTiming implements LessonSchedule, Comparable<LessonTiming> {

    public static final String MESSAGE_CONSTRAINTS =
            "Lesson timing should use DAYHHmm-HHmm (for example, Monday1800-1930), with end after start.";
    public static final String MESSAGE_INVALID_TIME =
            "Invalid time '%s'. Times must use 24-hour HHmm from 0000 to 2359.";
    private static final Pattern TIMING_PATTERN = Pattern.compile(
            "(?i)(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)"
                    + "(\\d{4})-(\\d{4})");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final DayOfWeek day;
    private final LocalTime startTime;
    private final LocalTime endTime;

    /** Creates a weekly timing from its command representation. */
    public LessonTiming(String value) {
        requireNonNull(value);
        Optional<String> validationError = getValidationError(value);
        checkArgument(validationError.isEmpty(), validationError.orElse(MESSAGE_CONSTRAINTS));
        Matcher matcher = TIMING_PATTERN.matcher(value);
        matcher.matches();
        day = DayOfWeek.valueOf(matcher.group(1).toUpperCase(Locale.ROOT));
        startTime = LocalTime.parse(matcher.group(2), TIME_FORMAT);
        endTime = LocalTime.parse(matcher.group(3), TIME_FORMAT);
    }

    /** Returns whether the supplied value is a valid weekly lesson timing. */
    public static boolean isValidLessonTiming(String test) {
        return getValidationError(test).isEmpty();
    }

    /** Returns a user-facing validation error, or an empty value when the timing is valid. */
    public static Optional<String> getValidationError(String test) {
        if (test == null) {
            return Optional.of(MESSAGE_CONSTRAINTS);
        }
        Matcher matcher = TIMING_PATTERN.matcher(test);
        if (!matcher.matches()) {
            return Optional.of(MESSAGE_CONSTRAINTS);
        }
        try {
            LocalTime start = LocalTime.parse(matcher.group(2), TIME_FORMAT);
            try {
                LocalTime end = LocalTime.parse(matcher.group(3), TIME_FORMAT);
                return end.isAfter(start) ? Optional.empty() : Optional.of(MESSAGE_CONSTRAINTS);
            } catch (DateTimeParseException exception) {
                return Optional.of(String.format(MESSAGE_INVALID_TIME, matcher.group(3)));
            }
        } catch (DateTimeParseException exception) {
            return Optional.of(String.format(MESSAGE_INVALID_TIME, matcher.group(2)));
        }
    }

    @Override
    public ScheduleType getType() {
        return ScheduleType.WEEKLY;
    }

    @Override
    public DayOfWeek getDay() {
        return day;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    @Override
    public boolean occursOn(java.time.LocalDate date) {
        requireNonNull(date);
        return date.getDayOfWeek() == day;
    }

    public boolean startsAt(DayOfWeek expectedDay, LocalTime expectedTime) {
        return day == expectedDay && startTime.equals(expectedTime);
    }

    /**
     * Returns whether this timing overlaps {@code other} on the same day.
     * Timings that only meet at an endpoint are not considered overlapping.
     */
    public boolean overlapsWith(LessonTiming other) {
        return LessonSchedule.super.overlapsWith(other);
    }

    @Override
    public int compareTo(LessonTiming other) {
        int dayComparison = Integer.compare(day.getValue(), other.day.getValue());
        if (dayComparison != 0) {
            return dayComparison;
        }
        int startComparison = startTime.compareTo(other.startTime);
        return startComparison != 0 ? startComparison : endTime.compareTo(other.endTime);
    }

    @Override
    public String toString() {
        String displayDay = day.toString().substring(0, 1)
                + day.toString().substring(1).toLowerCase(Locale.ROOT);
        return displayDay + " " + startTime.format(DISPLAY_TIME_FORMAT) + "-" + endTime.format(DISPLAY_TIME_FORMAT);
    }

    /** Returns the compact form used in JSON and commands. */
    @Override
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
