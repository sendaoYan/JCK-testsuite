/*
 * @test
 * @summary java.time.Year API conformance: getValue, isLeap, length, and atDay.
 * @run main YearTest
 */

import java.time.LocalDate;
import java.time.Year;

public class YearTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testYear() {
        Year y = Year.of(2024);
        check(y.getValue() == 2024, "getValue");
        check(y.isLeap() && !Year.of(2023).isLeap(), "isLeap");
        check(y.length() == 366 && Year.of(2023).length() == 365, "length");
        check(y.atDay(60).equals(LocalDate.of(2024, 2, 29)), "atDay");
    }

    public static void main(String[] args) {
        testYear();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
