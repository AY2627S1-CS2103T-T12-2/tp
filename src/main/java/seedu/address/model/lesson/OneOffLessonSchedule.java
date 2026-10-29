package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;
import static seedu.address.commons.util.AppUtil.checkArgument;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** A lesson schedule that occurs once on an exact date. */
public final class OneOffLessonSchedule implements LessonSchedule, Comparable<OneOffLessonSchedule> {

    public static final String MESSAGE_CONSTRAINTS =
            "One-off class timing should use yyyy-MM-ddTHHmm-HHmm "
                    + "(for example, 2026-10-20T1800-1930), with end after start.";
    public static final String MESSAGE_INVALID_DATE =
            "Invalid date '%s'. Enter a real calendar date in yyyy-MM-dd format.";
    public static final String MESSAGE_INVALID_TIME =
            "Invalid time '%s'. Times must use 24-hour HHmm from 0000 to 2359.";
    private static final Pattern TIMING_PATTERN = Pattern.compile(
            "(\\d{4}-\\d{2}-\\d{2})T(\\d{4})-(\\d{4})");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm")
            .withResolverStyle(ResolverStyle.STRICT);
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT =
            DateTimeFormatter.ofPattern("d MMM uuuu", Locale.ENGLISH);
    private static final DateTimeFormatter DISPLAY_TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm");

    private final LocalDate date;
    private final LocalTime startTime;
    private final LocalTime endTime;

    /** Creates a one-off schedule from its command representation. */
    public OneOffLessonSchedule(String value) {
        requireNonNull(value);
        Optional<String> validationError = getValidationError(value);
        checkArgument(validationError.isEmpty(), validationError.orElse(MESSAGE_CONSTRAINTS));
        Matcher matcher = TIMING_PATTERN.matcher(value);
        matcher.matches();
        date = LocalDate.parse(matcher.group(1));
        startTime = LocalTime.parse(matcher.group(2), TIME_FORMAT);
        endTime = LocalTime.parse(matcher.group(3), TIME_FORMAT);
    }

    /** Returns whether {@code test} is a valid one-off schedule representation. */
    public static boolean isValidOneOffLessonSchedule(String test) {
        return getValidationError(test).isEmpty();
    }

    /** Returns a user-facing validation error, or an empty value when the schedule is valid. */
    public static Optional<String> getValidationError(String test) {
        if (test == null) {
            return Optional.of(MESSAGE_CONSTRAINTS);
        }
        Matcher matcher = TIMING_PATTERN.matcher(test);
        if (!matcher.matches()) {
            return Optional.of(MESSAGE_CONSTRAINTS);
        }
        try {
            LocalDate.parse(matcher.group(1));
        } catch (DateTimeParseException exception) {
            return Optional.of(String.format(MESSAGE_INVALID_DATE, matcher.group(1)));
        }
        LocalTime start;
        try {
            start = LocalTime.parse(matcher.group(2), TIME_FORMAT);
        } catch (DateTimeParseException exception) {
            return Optional.of(String.format(MESSAGE_INVALID_TIME, matcher.group(2)));
        }
        try {
            LocalTime end = LocalTime.parse(matcher.group(3), TIME_FORMAT);
            return end.isAfter(start) ? Optional.empty() : Optional.of(MESSAGE_CONSTRAINTS);
        } catch (DateTimeParseException exception) {
            return Optional.of(String.format(MESSAGE_INVALID_TIME, matcher.group(3)));
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
        if (dateComparison != 0) {
            return dateComparison;
        }
        int startComparison = startTime.compareTo(other.startTime);
        return startComparison != 0 ? startComparison : endTime.compareTo(other.endTime);
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
