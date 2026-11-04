/*
 * @test
 * @summary java.time.format.DateTimeFormatterBuilder API conformance: compose a
 *          year-month formatter.
 * @run main DateTimeFormatterBuilderTest
 */

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

public class DateTimeFormatterBuilderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDateTimeFormatterBuilder() {
        DateTimeFormatter f =
            new DateTimeFormatterBuilder()
                .appendValue(ChronoField.YEAR)
                .appendLiteral('-')
                .appendValue(ChronoField.MONTH_OF_YEAR, 2)
                .toFormatter();
        check("2024-06".equals(LocalDate.of(2024, 6, 1).format(f)), "builder format");
    }

    public static void main(String[] args) {
        testDateTimeFormatterBuilder();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
