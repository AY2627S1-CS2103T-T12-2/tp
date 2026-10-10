package seedu.address.model.timetable;

import static java.util.Objects.requireNonNull;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Objects;

/** An immutable day group displayed in the timetable. */
public class TimetableDay {

    private final DayOfWeek day;
    private final List<TimetableEntry> entries;

    /** Creates a day group, which may contain no entries. */
    public TimetableDay(DayOfWeek day, List<TimetableEntry> entries) {
        this.day = requireNonNull(day);
        this.entries = List.copyOf(requireNonNull(entries));
    }

    public DayOfWeek getDay() {
        return day;
    }

    public List<TimetableEntry> getEntries() {
        return entries;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof TimetableDay otherDay
                && day == otherDay.day && entries.equals(otherDay.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(day, entries);
    }
}
