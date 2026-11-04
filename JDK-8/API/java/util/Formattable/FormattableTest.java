/*
 * @test
 * @summary java.util.Formattable API conformance: formatTo writes to a Formatter.
 * @run main FormattableTest
 */

import java.util.Formattable;
import java.util.Formatter;

public class FormattableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class F implements Formattable {
        public void formatTo(Formatter formatter, int flags, int width, int precision) {
            formatter.format("[%d,%d,%d]", flags, width, precision);
        }
    }

    static void testFormattable() {
        StringBuilder sb = new StringBuilder();
        new F().formatTo(new Formatter(sb), 0, -1, -1);
        check("[0,-1,-1]".equals(sb.toString()), "formatTo writes to the formatter");
    }

    public static void main(String[] args) {
        testFormattable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
