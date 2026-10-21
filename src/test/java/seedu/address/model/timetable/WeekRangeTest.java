package seedu.address.model.timetable;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;

public class WeekRangeTest {

    private static final LocalDate SUNDAY = LocalDate.of(2026, 10, 18);

    @Test
    public void containing_everyWeekday_returnsSameCompleteWeek() {
        List<LocalDate> expectedDates = List.of(
                LocalDate.of(2026, 10, 18),
                LocalDate.of(2026, 10, 19),
                LocalDate.of(2026, 10, 20),
                LocalDate.of(2026, 10, 21),
                LocalDate.of(2026, 10, 22),
                LocalDate.of(2026, 10, 23),
                LocalDate.of(2026, 10, 24));

        for (int dayOffset = 0; dayOffset < 7; dayOffset++) {
            WeekRange range = WeekRange.containing(SUNDAY.plusDays(dayOffset));
            assertEquals(SUNDAY, range.getStart());
            assertEquals(LocalDate.of(2026, 10, 24), range.getEnd());
            assertEquals(expectedDates, range.getDates());
        }
    }

    @Test
    public void containing_calendarTransitions_returnsSevenConsecutiveDates() {
        assertEquals(LocalDate.of(2026, 11, 29),
                WeekRange.containing(LocalDate.of(2026, 12, 1)).getStart());
        assertEquals(LocalDate.of(2027, 1, 2),
                WeekRange.containing(LocalDate.of(2026, 12, 31)).getEnd());
        assertTrue(WeekRange.containing(LocalDate.of(2024, 2, 29)).getDates()
                .contains(LocalDate.of(2024, 2, 29)));
    }

    @Test
    public void containing_unsupportedBoundaryDates_throwsDateTimeException() {
        assertThrows(DateTimeException.class, () -> WeekRange.containing(LocalDate.MIN));
        assertThrows(DateTimeException.class, () -> WeekRange.containing(LocalDate.MAX));
    }

    @Test
    public void adjacentWeeks_boundariesReturnEmptyOtherwiseReturnSevenDaysAway() {
        LocalDate earliestSunday = LocalDate.MIN.plusDays(
                Math.floorMod(DayOfWeek.SUNDAY.getValue() - LocalDate.MIN.getDayOfWeek().getValue(), 7));
        WeekRange earliestWeek = WeekRange.containing(earliestSunday);
        assertTrue(earliestWeek.previous().isEmpty());
        assertEquals(earliestWeek.getStart().plusDays(7), earliestWeek.next().orElseThrow().getStart());

        LocalDate latestSaturday = LocalDate.MAX.minusDays(
                Math.floorMod(LocalDate.MAX.getDayOfWeek().getValue() - DayOfWeek.SATURDAY.getValue(), 7));
        WeekRange latestWeek = WeekRange.containing(latestSaturday);
        assertTrue(latestWeek.next().isEmpty());
        assertEquals(latestWeek.getStart().minusDays(7), latestWeek.previous().orElseThrow().getStart());
        assertFalse(earliestWeek.equals(latestWeek));
    }
}
