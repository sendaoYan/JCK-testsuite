/*
 * @test
 * @summary java.util.logging.Handler conformance: white-box structure + black-box behavior.
 * @run main HandlerTest
 */

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.ErrorManager;
import java.util.logging.Filter;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class HandlerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void expectThrows(Class<? extends Throwable> type, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + type.getSimpleName() +
                    " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + type.getSimpleName() + " but none thrown");
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.Handler");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(Modifier.isProtected(c.getDeclaredConstructor().getModifiers()), "protected constructor");
        check(Modifier.isAbstract(c.getMethod("publish", LogRecord.class).getModifiers()), "publish abstract");
        check(Modifier.isAbstract(c.getMethod("flush").getModifiers()), "flush abstract");
        check(Modifier.isAbstract(c.getMethod("close").getModifiers()), "close abstract");

        check(hasMethod(c, "setFormatter", Formatter.class), "setFormatter");
        check(hasMethod(c, "getFormatter"), "getFormatter");
        check(hasMethod(c, "setEncoding", String.class), "setEncoding");
        check(hasMethod(c, "getEncoding"), "getEncoding");
        check(hasMethod(c, "setFilter", Filter.class), "setFilter");
        check(hasMethod(c, "getFilter"), "getFilter");
        check(hasMethod(c, "setErrorManager", ErrorManager.class), "setErrorManager");
        check(hasMethod(c, "getErrorManager"), "getErrorManager");
        check(hasMethod(c, "setLevel", Level.class), "setLevel");
        check(hasMethod(c, "getLevel"), "getLevel");
        check(hasMethod(c, "isLoggable", LogRecord.class), "isLoggable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        CapturingHandler h = new CapturingHandler();

        // defaults
        check(h.getLevel() == Level.ALL, "default level ALL");
        check(h.getFormatter() == null, "default formatter null");
        check(h.getFilter() == null, "default filter null");
        check(h.getErrorManager() != null, "default ErrorManager installed");
        check(h.getEncoding() == null, "default encoding null");

        // level set/get + null rejection
        h.setLevel(Level.WARNING);
        check(h.getLevel() == Level.WARNING, "set/getLevel");
        expectThrows(NullPointerException.class, () -> h.setLevel(null), "setLevel(null) NPE");

        // formatter set/get + null rejection
        Formatter fmt = new SimpleFormatter();
        h.setFormatter(fmt);
        check(h.getFormatter() == fmt, "set/getFormatter");
        expectThrows(NullPointerException.class, () -> h.setFormatter(null), "setFormatter(null) NPE");

        // filter set/get (null allowed)
        Filter f = r -> true;
        h.setFilter(f);
        check(h.getFilter() == f, "set/getFilter");
        h.setFilter(null);
        check(h.getFilter() == null, "setFilter(null) allowed");

        // error manager set/get + null rejection
        ErrorManager em = new ErrorManager();
        h.setErrorManager(em);
        check(h.getErrorManager() == em, "set/getErrorManager");
        expectThrows(NullPointerException.class, () -> h.setErrorManager(null), "setErrorManager(null) NPE");

        // encoding set/get + invalid rejection
        h.setEncoding("UTF-8");
        check("UTF-8".equals(h.getEncoding()), "set/getEncoding UTF-8");
        h.setEncoding(null);
        check(h.getEncoding() == null, "setEncoding(null) resets");
        expectThrows(UnsupportedEncodingException.class, () -> h.setEncoding("no-such-charset-xyz"),
                "setEncoding invalid throws UnsupportedEncodingException");

        // isLoggable reflects level and handles null record
        h.setLevel(Level.WARNING);
        h.setFilter(null);
        check(h.isLoggable(new LogRecord(Level.SEVERE, "x")), "isLoggable SEVERE above level");
        check(h.isLoggable(new LogRecord(Level.WARNING, "x")), "isLoggable WARNING at level");
        check(!h.isLoggable(new LogRecord(Level.INFO, "x")), "isLoggable INFO below level");
        check(!h.isLoggable(null), "isLoggable(null) false");

        // publish to a capturing handler (multi-record composition)
        h.setLevel(Level.ALL);
        h.setFilter(r -> r.getMessage() != null && r.getMessage().startsWith("keep"));
        h.publish(new LogRecord(Level.INFO, "keep 1"));
        h.publish(new LogRecord(Level.INFO, "drop 2"));
        h.publish(new LogRecord(Level.SEVERE, "keep 3"));
        check(h.records.size() == 2, "handler publish respects filter");
        check("keep 1".equals(h.records.get(0).getMessage()), "first published record");
        check("keep 3".equals(h.records.get(1).getMessage()), "second published record");
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
