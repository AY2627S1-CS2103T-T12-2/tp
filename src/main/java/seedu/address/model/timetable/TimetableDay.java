package seedu.address.model.timetable;

import static java.util.Objects.requireNonNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;

/** An immutable day group displayed in the timetable. */
public class TimetableDay {

    private final LocalDate date;
    private final List<TimetableEntry> entries;

    /** Creates a day group, which may contain no entries. */
    public TimetableDay(LocalDate date, List<TimetableEntry> entries) {
        this.date = requireNonNull(date);
        this.entries = List.copyOf(requireNonNull(entries));
    }

    public LocalDate getDate() {
        return date;
    }

    public DayOfWeek getDay() {
        return date.getDayOfWeek();
    }

    public List<TimetableEntry> getEntries() {
        return entries;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof TimetableDay otherDay
                && date.equals(otherDay.date) && entries.equals(otherDay.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, entries);
    }
}
