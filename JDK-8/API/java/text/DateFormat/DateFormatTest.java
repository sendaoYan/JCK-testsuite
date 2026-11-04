/*
 * @test
 * @summary java.text.DateFormat API conformance: factory-returned formatters produce a
 *          non-empty string.
 * @run main DateFormatTest
 */

import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateFormatTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDateFormat() {
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
        DateFormat df =
            DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM,
                                           Locale.US);
        String s = df.format(new Date(0));
        check(s != null && s.length() > 0, "format non-empty");
    }

    public static void main(String[] args) {
        testDateFormat();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
