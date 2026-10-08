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

/** A day and start time used to find lessons. */
public class LessonStart {

    public static final String MESSAGE_CONSTRAINTS =
            "Class search should use DAYHHmm (for example, Monday1800).";
    private static final Pattern PATTERN = Pattern.compile(
            "(?i)(Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday)"
                    + "((?:[01]\\d|2[0-3])[0-5]\\d)");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HHmm");

    private final DayOfWeek day;
    private final LocalTime time;

    /** Creates a lesson-start search value. */
    public LessonStart(String value) {
        requireNonNull(value);
        checkArgument(isValidLessonStart(value), MESSAGE_CONSTRAINTS);
        Matcher matcher = PATTERN.matcher(value);
        matcher.matches();
        day = DayOfWeek.valueOf(matcher.group(1).toUpperCase(Locale.ROOT));
        time = LocalTime.parse(matcher.group(2), TIME_FORMAT);
    }

    /** Returns whether the supplied value is a valid lesson-start search value. */
    public static boolean isValidLessonStart(String test) {
        Matcher matcher = PATTERN.matcher(test);
        if (!matcher.matches()) {
            return false;
        }
        try {
            LocalTime.parse(matcher.group(2), TIME_FORMAT);
            return true;
        } catch (DateTimeParseException exception) {
            return false;
        }
    }

    public boolean matches(LessonTiming timing) {
        return timing.startsAt(day, time);
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof LessonStart otherStart
                && day == otherStart.day && time.equals(otherStart.time);
    }

    @Override
    public int hashCode() {
        return java.util.Objects.hash(day, time);
    }

    @Override
    public String toString() {
        return day + time.format(TIME_FORMAT);
    }
}
