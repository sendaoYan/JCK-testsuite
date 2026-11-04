/*
 * @test
 * @summary java.util.logging.ConsoleHandler conformance: white-box structure + black-box behavior.
 * @run main ConsoleHandlerTest
 */

import java.lang.reflect.Modifier;
import java.util.logging.ConsoleHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

public class ConsoleHandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.ConsoleHandler");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(StreamHandler.class.isAssignableFrom(c), "extends StreamHandler");
        check(c.getSuperclass() == StreamHandler.class, "direct superclass StreamHandler");
        check(Handler.class.isAssignableFrom(c), "is a Handler");
        check(c.getConstructor() != null, "no-arg constructor");
        check(c.getMethod("publish", LogRecord.class) != null, "publish(LogRecord)");
        check(c.getMethod("close") != null, "close()");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Constructing is safe: the output stream (System.err) is only referenced,
        // never written until publish() is called. We never publish.
        ConsoleHandler h = new ConsoleHandler();
        check(h.getLevel() == Level.INFO, "default level INFO");
        check(h.getFormatter() instanceof SimpleFormatter, "default formatter SimpleFormatter");
        check(h.getEncoding() == null, "default encoding null");

        // isLoggable reflects the level; safe because it writes nothing.
        check(h.isLoggable(new LogRecord(Level.INFO, "x")), "isLoggable INFO true");
        check(h.isLoggable(new LogRecord(Level.SEVERE, "x")), "isLoggable SEVERE true");
        check(!h.isLoggable(new LogRecord(Level.FINE, "x")), "isLoggable FINE false");

        // level round-trip
        h.setLevel(Level.WARNING);
        check(h.getLevel() == Level.WARNING, "set/getLevel WARNING");

        // close() only flushes System.err; it must not throw.
        h.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
