/*
 * @test
 * @summary java.time.temporal.TemporalAccessor API conformance: field support and getLong.
 * @run main TemporalAccessorTest
 */

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;

public class TemporalAccessorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTemporalAccessor() {
        TemporalAccessor ta = LocalDate.of(2024, 6, 15);
        check(ta instanceof LocalDate, "LocalDate is a TemporalAccessor");
        check(ta.isSupported(ChronoField.DAY_OF_MONTH), "supports day of month");
        check(ta.getLong(ChronoField.YEAR) == 2024, "getLong year");
    }

    public static void main(String[] args) {
        testTemporalAccessor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
