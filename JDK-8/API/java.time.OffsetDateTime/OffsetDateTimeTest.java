/*
 * @test
 * @summary java.time.OffsetDateTime API conformance: of/getters, getOffset, toInstant, and
 *          plusDays.
 * @run main OffsetDateTimeTest
 */

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class OffsetDateTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOffsetDateTime() {
        OffsetDateTime odt = OffsetDateTime.of(2024, 3, 1, 10, 30, 0, 0, ZoneOffset.ofHours(2));
        check(odt.getYear() == 2024 && odt.getHour() == 10, "of/getters");
        check(odt.getOffset().equals(ZoneOffset.ofHours(2)), "getOffset");
        check(odt.toInstant().equals(Instant.parse("2024-03-01T08:30:00Z")), "toInstant");
        check(odt.plusDays(1).getDayOfMonth() == 2, "plusDays");
    }

    public static void main(String[] args) {
        testOffsetDateTime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
