/*
 * @test
 * @summary java.time.format.DateTimeFormatter API conformance: ofPattern format/parse, and
 *          ISO_LOCAL_DATE.
 * @run main DateTimeFormatterTest
 */

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class DateTimeFormatterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDateTimeFormatter() {
        DateTimeFormatter f = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate d = LocalDate.of(2024, 3, 1);
        check("2024-03-01".equals(d.format(f)), "format");
        check(f.parse("2024-03-01", LocalDate::from).equals(d), "parse with from");
        check("2024-03-01".equals(DateTimeFormatter.ISO_LOCAL_DATE.format(d)),
              "ISO_LOCAL_DATE format");
    }

    public static void main(String[] args) {
        testDateTimeFormatter();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
