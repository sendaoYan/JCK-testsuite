/*
 * @test
 * @summary java.util.Formatter conformance: white-box structure plus black-box behavior of
 *          format into an Appendable with explicit Locale, %d/%s/%x/%f/%05d/%b/%c/%%,
 *          out()/locale(), String.format(Locale), flush, and FormatterClosedException.
 * @run main FormatterTest
 */

import java.util.Formatter;
import java.util.FormatterClosedException;
import java.util.Locale;

public class FormatterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void assertThrows(Class<? extends Throwable> exType, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (exType.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (threw " + t + ")");
        }
        throw new AssertionError(msg + " (no exception)");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.Formatter");
        check(!c.isInterface(), "Formatter is a class");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "Formatter is final");
        check(java.io.Closeable.class.isAssignableFrom(c), "Formatter implements Closeable");
        check(java.io.Flushable.class.isAssignableFrom(c), "Formatter implements Flushable");
        c.getMethod("format", String.class, Object[].class);
        c.getMethod("format", Locale.class, String.class, Object[].class);
        c.getMethod("out");
        c.getMethod("locale");
        c.getMethod("close");
        passed++;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        testFormat();
        testStringFormat();
        testOutAndLocale();
        testClosed();
    }

    static void testFormat() {
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb, Locale.US);
        f.format("%d %s %x %.2f", 42, "hi", 255, 3.14159);
        f.close();
        check("42 hi ff 3.14".equals(sb.toString()), "format d/s/x/f");

        StringBuilder sb2 = new StringBuilder();
        Formatter f2 = new Formatter(sb2, Locale.ROOT);
        f2.format("%05d|%b|%c|%%|%s", 7, true, 'A', "tail");
        f2.close();
        check("00007|true|A|%|tail".equals(sb2.toString()), "format zero-pad/bool/char/percent/string");
    }

    static void testStringFormat() {
        check("3.14".equals(String.format(Locale.US, "%.2f", 3.14159)), "String.format(Locale)");
        check("00042".equals(String.format(Locale.ROOT, "%05d", 42)), "String.format zero-pad");
        check("42 hi".equals(String.format(Locale.ROOT, "%2$d %1$s", "hi", 42)),
              "String.format positional args");
    }

    static void testOutAndLocale() {
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb, Locale.US);
        check(f.out() == sb, "out() returns backend");
        check(f.locale() == Locale.US, "locale() returns constructor Locale");
        f.close();
    }

    static void testClosed() {
        StringBuilder sb = new StringBuilder();
        Formatter f = new Formatter(sb, Locale.US);
        f.close();
        assertThrows(FormatterClosedException.class,
                () -> f.format("%d", 1), "format after close throws FormatterClosedException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
