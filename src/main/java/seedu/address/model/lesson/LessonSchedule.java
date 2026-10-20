package seedu.address.model.lesson;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;

/** Describes when a lesson occurs. */
public interface LessonSchedule {

    ScheduleType getType();

    /** Returns the weekday on which this schedule occurs. */
    DayOfWeek getDay();

    LocalTime getStartTime();

    LocalTime getEndTime();

    /** Returns whether this schedule has an occurrence on {@code date}. */
    boolean occursOn(LocalDate date);

    /** Returns the compact representation used by commands and storage. */
    String toStorageString();
}
