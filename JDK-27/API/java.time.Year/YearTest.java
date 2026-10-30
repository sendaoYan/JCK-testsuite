/*
 * @test
 * @summary java.time.Year API conformance: value, leap, length, and atDay.
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
        check(y.getValue() == 2024, "value");
        check(y.isLeap(), "isLeap 2024");
        check(!Year.of(2023).isLeap(), "2023 not leap");
        check(y.length() == 366, "length 366");
        check(Year.of(2023).length() == 365, "length 365");
        check(y.atDay(60).equals(LocalDate.of(2024, 2, 29)), "atDay 60 leap");
        check(Year.of(2023).atDay(60).equals(LocalDate.of(2023, 3, 1)),
              "atDay 60 non-leap");
    }

    public static void main(String[] args) {
        testYear();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
