package seedu.address.model.timetable;

import static java.util.Objects.requireNonNull;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

/** An immutable, complete Sunday-to-Saturday date range. */
public final class WeekRange {

    private static final long MIN_EPOCH_DAY = LocalDate.MIN.toEpochDay();
    private static final long MAX_EPOCH_DAY = LocalDate.MAX.toEpochDay();

    private final LocalDate start;
    private final LocalDate end;

    private WeekRange(long startEpochDay) {
        start = LocalDate.ofEpochDay(startEpochDay);
        end = LocalDate.ofEpochDay(startEpochDay + 6);
    }

    /**
     * Returns the complete Sunday-to-Saturday week containing {@code anchorDate}.
     *
     * @throws DateTimeException if the containing complete week is outside the supported
     *     {@link LocalDate} range
     */
    public static WeekRange containing(LocalDate anchorDate) {
        requireNonNull(anchorDate);
        long daysAfterSunday = anchorDate.getDayOfWeek().getValue() % 7;
        long startEpochDay = anchorDate.toEpochDay() - daysAfterSunday;
        return fromStartEpochDay(startEpochDay);
    }

    public LocalDate getStart() {
        return start;
    }

    public LocalDate getEnd() {
        return end;
    }

    /** Returns all seven dates in this week, ordered from Sunday to Saturday. */
    public List<LocalDate> getDates() {
        return IntStream.range(0, 7)
                .mapToObj(start::plusDays)
                .toList();
    }

    /** Returns the preceding complete week, or an empty value at the lower date boundary. */
    public Optional<WeekRange> previous() {
        long previousStartEpochDay = start.toEpochDay() - 7;
        return isCompleteWeekStart(previousStartEpochDay)
                ? Optional.of(new WeekRange(previousStartEpochDay))
                : Optional.empty();
    }

    /** Returns the following complete week, or an empty value at the upper date boundary. */
    public Optional<WeekRange> next() {
        long nextStartEpochDay = start.toEpochDay() + 7;
        return isCompleteWeekStart(nextStartEpochDay)
                ? Optional.of(new WeekRange(nextStartEpochDay))
                : Optional.empty();
    }

    private static WeekRange fromStartEpochDay(long startEpochDay) {
        if (!isCompleteWeekStart(startEpochDay)) {
            throw new DateTimeException("The date does not belong to a complete representable week");
        }
        return new WeekRange(startEpochDay);
    }

    private static boolean isCompleteWeekStart(long startEpochDay) {
        return startEpochDay >= MIN_EPOCH_DAY
                && startEpochDay + 6 <= MAX_EPOCH_DAY
                && LocalDate.ofEpochDay(startEpochDay).getDayOfWeek() == DayOfWeek.SUNDAY;
    }

    @Override
    public boolean equals(Object other) {
        return other == this || other instanceof WeekRange otherRange
                && start.equals(otherRange.start);
    }

    @Override
    public int hashCode() {
        return Objects.hash(start);
    }
}
