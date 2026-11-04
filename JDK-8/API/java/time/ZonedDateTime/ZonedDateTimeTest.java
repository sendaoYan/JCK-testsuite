/*
 * @test
 * @summary java.time.ZonedDateTime API conformance: of/getters, getOffset, toInstant,
 *          plusDays, isBefore, and withZoneSameInstant.
 * @run main ZonedDateTimeTest
 */

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class ZonedDateTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZonedDateTime() {
        ZonedDateTime zdt = ZonedDateTime.of(2024, 3, 1, 12, 0, 0, 0, ZoneOffset.UTC);
        check(zdt.getYear() == 2024 && zdt.getHour() == 12, "of/getters");
        check(zdt.getOffset().equals(ZoneOffset.UTC), "getOffset");
        check(zdt.toInstant().equals(Instant.parse("2024-03-01T12:00:00Z")), "toInstant");
        check(zdt.plusDays(1).getDayOfMonth() == 2, "plusDays");
        check(zdt.isBefore(ZonedDateTime.of(2024, 3, 2, 0, 0, 0, 0, ZoneOffset.UTC)), "isBefore");
        check(zdt.withZoneSameInstant(ZoneOffset.ofHours(1)).getHour() == 13, "withZoneSameInstant");
    }

    public static void main(String[] args) {
        testZonedDateTime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
