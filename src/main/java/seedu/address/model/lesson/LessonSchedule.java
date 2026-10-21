package seedu.address.model.lesson;

import static java.util.Objects.requireNonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;

/** Describes when a lesson occurs. */
public interface LessonSchedule {

    ScheduleType getType();

    /** Returns the weekday on which this schedule occurs. */
    DayOfWeek getDay();

    LocalTime getStartTime();

    LocalTime getEndTime();

    /** Returns whether this schedule has an occurrence on {@code date}. */
    boolean occursOn(LocalDate date);

    /** Returns the exact date for a one-off schedule, or an empty value for a recurring schedule. */
    default Optional<LocalDate> getDate() {
        return Optional.empty();
    }

    /**
     * Returns whether this schedule and {@code other} occur on a common date with overlapping times.
     * Timings that only meet at an endpoint do not overlap.
     */
    default boolean overlapsWith(LessonSchedule other) {
        requireNonNull(other);

        boolean occursOnSameDate;
        if (getDate().isPresent()) {
            occursOnSameDate = other.occursOn(getDate().get());
        } else if (other.getDate().isPresent()) {
            occursOnSameDate = occursOn(other.getDate().get());
        } else {
            occursOnSameDate = getDay() == other.getDay();
        }

        return occursOnSameDate
                && getStartTime().isBefore(other.getEndTime())
                && other.getStartTime().isBefore(getEndTime());
    }

    /** Returns the compact representation used by commands and storage. */
    String toStorageString();
}
