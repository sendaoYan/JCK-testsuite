/*
 * @test
 * @summary java.time.temporal.IsoFields conformance: white-box structure + black-box behavior.
 * @run main IsoFieldsTest
 */

import java.lang.reflect.Modifier;
import java.time.LocalDate;
import java.time.temporal.IsoFields;
import java.time.temporal.TemporalField;
import java.time.temporal.TemporalUnit;
import java.time.temporal.ValueRange;

public class IsoFieldsTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.time.temporal.IsoFields");
        check(Modifier.isFinal(c.getModifiers()), "is final utility class");
        check(c.getConstructors().length == 0, "no public constructor");
        check(IsoFields.QUARTER_OF_YEAR instanceof TemporalField, "QUARTER_OF_YEAR is a TemporalField");
        check(IsoFields.DAY_OF_QUARTER instanceof TemporalField, "DAY_OF_QUARTER is a TemporalField");
        check(IsoFields.WEEK_OF_WEEK_BASED_YEAR instanceof TemporalField, "WEEK_OF_WEEK_BASED_YEAR is a TemporalField");
        check(IsoFields.WEEK_BASED_YEAR instanceof TemporalField, "WEEK_BASED_YEAR is a TemporalField");
        check(IsoFields.WEEK_BASED_YEARS instanceof TemporalUnit, "WEEK_BASED_YEARS is a TemporalUnit");
        check(IsoFields.QUARTER_YEARS instanceof TemporalUnit, "QUARTER_YEARS is a TemporalUnit");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // quarter of year
        check(IsoFields.QUARTER_OF_YEAR.getFrom(LocalDate.of(2024, 1, 1)) == 1, "QUARTER_OF_YEAR Jan = 1");
        check(IsoFields.QUARTER_OF_YEAR.getFrom(LocalDate.of(2024, 7, 1)) == 3, "QUARTER_OF_YEAR Jul = 3");
        check(IsoFields.QUARTER_OF_YEAR.getFrom(LocalDate.of(2024, 10, 1)) == 4, "QUARTER_OF_YEAR Oct = 4");
        check(IsoFields.QUARTER_OF_YEAR.range().equals(ValueRange.of(1, 4)), "QUARTER_OF_YEAR range 1..4");

        // day of quarter (leap vs non-leap)
        check(IsoFields.DAY_OF_QUARTER.getFrom(LocalDate.of(2024, 1, 1)) == 1, "DAY_OF_QUARTER first day");
        check(IsoFields.DAY_OF_QUARTER.getFrom(LocalDate.of(2024, 3, 31)) == 91, "DAY_OF_QUARTER end of Q1 2024 (leap) = 91");
        check(IsoFields.DAY_OF_QUARTER.getFrom(LocalDate.of(2023, 3, 31)) == 90, "DAY_OF_QUARTER end of Q1 2023 (non-leap) = 90");
        check(IsoFields.DAY_OF_QUARTER.range().getMinimum() == 1, "DAY_OF_QUARTER range min 1");
        check(IsoFields.DAY_OF_QUARTER.range().getSmallestMaximum() == 90, "DAY_OF_QUARTER smallest max 90");
        check(IsoFields.DAY_OF_QUARTER.range().getMaximum() == 92, "DAY_OF_QUARTER max 92");

        // week-based-year fields (2024-01-01 is a Monday, ISO week-based-year 2024)
        check(IsoFields.WEEK_OF_WEEK_BASED_YEAR.getFrom(LocalDate.of(2024, 1, 1)) == 1, "WOWBY 2024-01-01 = 1");
        check(IsoFields.WEEK_BASED_YEAR.getFrom(LocalDate.of(2024, 1, 1)) == 2024, "WBY 2024-01-01 = 2024");
        check(IsoFields.WEEK_BASED_YEAR.getFrom(LocalDate.of(2024, 12, 30)) == 2025, "WBY 2024-12-30 = 2025");

        // quarter-years unit arithmetic
        check(IsoFields.QUARTER_YEARS.addTo(LocalDate.of(2024, 1, 1), 1).equals(LocalDate.of(2024, 4, 1)), "QUARTER_YEARS.addTo +1 -> Apr");
        check(IsoFields.QUARTER_YEARS.between(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 7, 1)) == 2, "QUARTER_YEARS.between = 2");
        check(IsoFields.WEEK_BASED_YEARS.isDateBased(), "WEEK_BASED_YEARS is date-based");
        check(IsoFields.WEEK_BASED_YEARS.isDurationEstimated(), "WEEK_BASED_YEARS duration estimated");
        check(IsoFields.QUARTER_YEARS.isDurationEstimated(), "QUARTER_YEARS duration estimated");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
