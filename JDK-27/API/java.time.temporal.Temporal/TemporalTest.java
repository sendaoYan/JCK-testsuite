/*
 * @test
 * @summary java.time.temporal.Temporal API conformance: with/plus via a LocalDate.
 * @run main TemporalTest
 */

import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.time.temporal.Temporal;

public class TemporalTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testTemporal() {
        Temporal t = LocalDate.of(2024, 6, 15);
        check(t instanceof LocalDate, "LocalDate is a Temporal");
        check(t.isSupported(ChronoField.YEAR), "supports year");
        check(t.with(ChronoField.YEAR, 2025).equals(LocalDate.of(2025, 6, 15)),
              "with year");
        check(t.plus(1, ChronoUnit.DAYS).equals(LocalDate.of(2024, 6, 16)),
              "plus day");
    }

    public static void main(String[] args) {
        testTemporal();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
