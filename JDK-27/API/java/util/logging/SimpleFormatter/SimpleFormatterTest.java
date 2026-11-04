/*
 * @test
 * @summary java.util.logging.SimpleFormatter conformance: white-box structure + black-box behavior.
 * @run main SimpleFormatterTest
 */

import java.lang.reflect.Modifier;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class SimpleFormatterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.SimpleFormatter");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(Formatter.class.isAssignableFrom(c), "extends Formatter");
        check(c.getSuperclass() == Formatter.class, "direct superclass Formatter");
        check(c.getConstructor() != null, "no-arg constructor");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        SimpleFormatter f = new SimpleFormatter();

        LogRecord r = new LogRecord(Level.INFO, "hello world");
        r.setLoggerName("jck.simple");
        r.setMillis(1700000000000L);
        String out = f.format(r);
        check(out.contains("hello world"), "format contains message");
        check(out.contains("jck.simple"), "format contains logger name");

        // parameter substitution through formatMessage used by format
        LogRecord param = new LogRecord(Level.WARNING, "a={0} b={1}");
        param.setLoggerName("jck.simple");
        param.setParameters(new Object[] {Integer.valueOf(1), Integer.valueOf(2)});
        param.setMillis(1700000000000L);
        String paramOut = f.format(param);
        check(paramOut.contains("a=1 b=2"), "format substitutes {0}/{1}");

        check("".equals(f.getHead(null)), "getHead default empty");
        check("".equals(f.getTail(null)), "getTail default empty");

        // a thrown throwable is appended (contains its class name)
        LogRecord thrown = new LogRecord(Level.SEVERE, "boom");
        thrown.setLoggerName("jck.simple");
        thrown.setMillis(1700000000000L);
        thrown.setThrown(new IllegalStateException("detail"));
        String thrownOut = f.format(thrown);
        check(thrownOut.contains("boom"), "format with thrown keeps message");
        check(thrownOut.contains("IllegalStateException"), "format includes throwable type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
