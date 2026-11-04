/*
 * @test
 * @summary java.util.Formatter API conformance: format into an Appendable with explicit
 *          Locale, and String.format(Locale, ...).
 * @run main FormatterTest
 */

import java.util.Formatter;
import java.util.Locale;

public class FormatterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testFormat() {
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb, Locale.US);
        f.format("%d %s %x %.2f", 42, "hi", 255, 3.14159);
        f.close();
        check("42 hi ff 3.14".equals(sb.toString()), "format d/s/x/f");
    }

    static void testStringFormat() {
        check("3.14".equals(String.format(Locale.US, "%.2f", 3.14159)), "String.format(Locale)");
    }

    public static void main(String[] args) {
        testFormat();
        testStringFormat();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
