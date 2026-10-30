/*
 * @test
 * @summary java.time.LocalDateTime API conformance: of/getters, plusDays, toLocalDate/
 *          toLocalTime, and isBefore.
 * @run main LocalDateTimeTest
 */

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class LocalDateTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testLocalDateTime() {
        LocalDateTime dt = LocalDateTime.of(2024, 3, 1, 12, 0);
        check(dt.getYear() == 2024 && dt.getHour() == 12, "of/getters");
        check(dt.plusDays(1).equals(LocalDateTime.of(2024, 3, 2, 12, 0)), "plusDays");
        check(dt.toLocalDate().equals(LocalDate.of(2024, 3, 1)), "toLocalDate");
        check(dt.toLocalTime().equals(LocalTime.of(12, 0)), "toLocalTime");
        check(dt.isBefore(LocalDateTime.of(2024, 3, 2, 0, 0)), "isBefore");
    }

    public static void main(String[] args) {
        testLocalDateTime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
