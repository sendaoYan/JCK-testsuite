/*
 * @test
 * @summary java.time.ZonedDateTime API conformance: zone, accessors, and instant roundtrip.
 * @run main ZonedDateTimeTest
 */

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class ZonedDateTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testZonedDateTime() {
        ZonedDateTime zdt =
            ZonedDateTime.of(2024, 6, 15, 10, 30, 0, 0, ZoneId.of("UTC"));
        check(zdt.getZone().equals(ZoneId.of("UTC")), "zone");
        check(zdt.getYear() == 2024, "year");
        check(zdt.toLocalDate().equals(LocalDate.of(2024, 6, 15)), "toLocalDate");
        check(zdt.toInstant().atZone(zdt.getZone()).equals(zdt), "instant roundtrip");
    }

    public static void main(String[] args) {
        testZonedDateTime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
