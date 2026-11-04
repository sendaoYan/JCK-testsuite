/*
 * @test
 * @summary java.time.temporal.TemporalField conformance: via ChronoField.DAY_OF_MONTH.
 * @run main TemporalFieldTest
 */

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalField;

public class TemporalFieldTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTemporalField() {
        TemporalField f = ChronoField.DAY_OF_MONTH;
        check(f.isDateBased(), "isDateBased");
        check(!f.isTimeBased(), "not isTimeBased");
        check(f.isSupportedBy(LocalDate.now()), "isSupportedBy LocalDate");
        check(f.getBaseUnit() == ChronoUnit.DAYS, "getBaseUnit DAYS");
        check(f.getRangeUnit() == ChronoUnit.MONTHS, "getRangeUnit MONTHS");
        check(f.range().getMinimum() == 1, "range min 1");
        check(f.range().getMaximum() == 31, "range max 31");
        check(f.rangeRefinedBy(LocalDate.of(2020, 2, 1)).getMaximum() == 29, "rangeRefinedBy Feb 2020");
        check(f.getFrom(LocalDate.of(2020, 1, 15)) == 15, "getFrom");
        check(f.adjustInto(LocalDate.of(2020, 1, 1), 20).equals(LocalDate.of(2020, 1, 20)),
              "adjustInto");
    }

    public static void main(String[] args) {
        testTemporalField();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
