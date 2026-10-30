/*
 * @test
 * @summary java.time.OffsetDateTime API conformance: accessors, offset, and instant
 *          roundtrip.
 * @run main OffsetDateTimeTest
 */

import java.time.LocalDateTime;
import java.time.Month;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public class OffsetDateTimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testOffsetDateTime() {
        OffsetDateTime odt =
            OffsetDateTime.of(2024, 6, 15, 10, 30, 0, 0, ZoneOffset.ofHours(2));
        check(odt.getYear() == 2024, "year");
        check(odt.getMonth() == Month.JUNE, "month");
        check(odt.getDayOfMonth() == 15, "day of month");
        check(odt.getOffset().equals(ZoneOffset.ofHours(2)), "offset");
        check(odt.toLocalDateTime().equals(LocalDateTime.of(2024, 6, 15, 10, 30)),
              "toLocalDateTime");
        check(odt.toInstant().atOffset(odt.getOffset()).equals(odt), "instant roundtrip");
    }

    public static void main(String[] args) {
        testOffsetDateTime();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
