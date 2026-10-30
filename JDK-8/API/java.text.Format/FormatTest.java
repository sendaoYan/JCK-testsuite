/*
 * @test
 * @summary java.text.Format API conformance: base class for NumberFormat and format via a
 *          Format reference.
 * @run main FormatTest
 */

import java.text.Format;
import java.text.NumberFormat;
import java.util.Locale;

public class FormatTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFormat() {
        Format f = NumberFormat.getIntegerInstance(Locale.US);
        check(f instanceof NumberFormat, "NumberFormat is a Format");
        check("42".equals(f.format(42)), "format via Format reference");
    }

    public static void main(String[] args) {
        testFormat();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
