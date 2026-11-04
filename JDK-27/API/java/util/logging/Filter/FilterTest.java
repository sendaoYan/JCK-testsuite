/*
 * @test
 * @summary java.util.logging.Filter conformance: white-box structure + black-box behavior.
 * @run main FilterTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Filter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

public class FilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.Filter");
        check(c.isInterface(), "Filter is an interface");
        check(Modifier.isPublic(c.getModifiers()), "Filter is public");
        check(Modifier.isPublic(c.getMethod("isLoggable", LogRecord.class).getModifiers())
                && Modifier.isAbstract(c.getMethod("isLoggable", LogRecord.class).getModifiers()),
                "isLoggable(LogRecord) is public abstract");
        check(c.isAnnotationPresent(FunctionalInterface.class), "Filter is @FunctionalInterface");
        check(hasMethod(c, "isLoggable", LogRecord.class), "single abstract method isLoggable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // lambda predicate
        Filter acceptsInfo = r -> r.getLevel().intValue() >= Level.INFO.intValue();
        check(acceptsInfo.isLoggable(new LogRecord(Level.INFO, "hello")), "lambda true for INFO");
        check(acceptsInfo.isLoggable(new LogRecord(Level.SEVERE, "x")), "lambda true for SEVERE");
        check(!acceptsInfo.isLoggable(new LogRecord(Level.FINE, "x")), "lambda false for FINE");

        // method-reference style (contains substring)
        Filter containsLog = r -> r.getMessage() != null && r.getMessage().contains("log");
        check(containsLog.isLoggable(new LogRecord(Level.INFO, "log this")), "contains -> true");
        check(!containsLog.isLoggable(new LogRecord(Level.INFO, "skip")), "contains -> false");

        // composition with Handler.setFilter to actually filter published records
        CapturingHandler h = new CapturingHandler();
        h.setFilter(r -> r.getMessage() != null && r.getMessage().startsWith("keep"));
        h.publish(new LogRecord(Level.INFO, "keep one"));
        h.publish(new LogRecord(Level.INFO, "drop me"));
        h.publish(new LogRecord(Level.WARNING, "keep two"));
        check(h.records.size() == 2, "handler filter dropped non-matching record");
        check("keep one".equals(h.records.get(0).getMessage()), "first kept message");
        check("keep two".equals(h.records.get(1).getMessage()), "second kept message");
    }

    static final class CapturingHandler extends Handler {
        final List<LogRecord> records = new ArrayList<>();
        public void publish(LogRecord record) {
            if (!isLoggable(record)) return;
            records.add(record);
        }
        public void flush() { }
        public void close() { }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
