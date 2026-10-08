package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.DayOfWeek;

import org.junit.jupiter.api.Test;

public class TimetableDayCardTest {

    @Test
    public void formatDay_dayOfWeek_returnsCapitalisedEnglishName() {
        assertEquals("Monday", TimetableDayCard.formatDay(DayOfWeek.MONDAY));
        assertEquals("Saturday", TimetableDayCard.formatDay(DayOfWeek.SATURDAY));
    }
}
