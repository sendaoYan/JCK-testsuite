/*
 * @test
 * @summary java.text.SimpleDateFormat API conformance: deterministic format/parse of the
 *          epoch under a fixed pattern, Locale.US, and UTC.
 * @run main SimpleDateFormatTest
 */

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class SimpleDateFormatTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSimpleDateFormat() throws Exception {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        sdf.setTimeZone(tz);
        check("1970-01-01 00:00:00".equals(sdf.format(new Date(0))), "format epoch");
        check(sdf.parse("1970-01-01 00:00:00").getTime() == 0, "parse epoch");
    }

    public static void main(String[] args) throws Exception {
        testSimpleDateFormat();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
