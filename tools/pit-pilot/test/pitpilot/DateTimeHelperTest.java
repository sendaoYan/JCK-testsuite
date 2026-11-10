package pitpilot;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.time.DayOfWeek;
import java.time.LocalDate;
import org.junit.Test;

public class DateTimeHelperTest {

    @Test
    public void leapYearsIncludingCenturyBoundary() {
        assertTrue(DateTimeHelper.isLeapYear(LocalDate.of(2024, 1, 1)));
        assertTrue(DateTimeHelper.isLeapYear(LocalDate.of(2000, 1, 1)));
        assertFalse(DateTimeHelper.isLeapYear(LocalDate.of(2023, 1, 1)));
        assertFalse(DateTimeHelper.isLeapYear(LocalDate.of(1900, 1, 1)));
    }

    @Test
    public void monthRoundTripPreservesOrdinaryDates() {
        assertTrue(DateTimeHelper.roundTripsMonth(LocalDate.of(2024, 3, 15), 2));
        assertTrue(DateTimeHelper.roundTripsMonth(LocalDate.of(2024, 3, 15), 0));
    }

    @Test
    public void monthRoundTripBreaksAtMonthEndClamping() {
        // 1/31 +1 月 -> 2/29，再 -1 月 -> 1/29，不等于 1/31
        assertFalse(DateTimeHelper.roundTripsMonth(LocalDate.of(2024, 1, 31), 1));
    }

    @Test
    public void daysBetweenForwardBackwardAndZero() {
        assertTrue(DateTimeHelper.daysBetweenEquals(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), 30));
        assertTrue(DateTimeHelper.daysBetweenEquals(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 1), 0));
        assertTrue(DateTimeHelper.daysBetweenEquals(LocalDate.of(2024, 1, 31), LocalDate.of(2024, 1, 1), -30));
        assertFalse(DateTimeHelper.daysBetweenEquals(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 1, 31), 31));
    }

    @Test
    public void weekendClassificationAllSevenDays() {
        assertTrue(DateTimeHelper.isWeekend(DayOfWeek.SATURDAY));
        assertTrue(DateTimeHelper.isWeekend(DayOfWeek.SUNDAY));
        for (DayOfWeek d : new DayOfWeek[]{
                DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY, DayOfWeek.FRIDAY}) {
            assertFalse(DateTimeHelper.isWeekend(d));
        }
    }
}
