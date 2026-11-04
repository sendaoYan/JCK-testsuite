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


    // ===================== 契约深化：函数式 lambda 门控/与 Logger 集成往返 =====================
    static void testContracts() {
        java.util.logging.LogRecord ok = new java.util.logging.LogRecord(java.util.logging.Level.INFO, "ok-1");
        java.util.logging.LogRecord no = new java.util.logging.LogRecord(java.util.logging.Level.INFO, "no-1");
        java.util.logging.Filter f = r -> r.getMessage().startsWith("ok");
        check(f.isLoggable(ok) && !f.isLoggable(no), "lambda Filter discriminates");
        // 与 Logger 集成：setFilter/getFilter 往返 + 移除
        java.util.logging.Logger l = java.util.logging.Logger.getLogger("jck.filter.roundtrip");
        l.setFilter(f);
        check(l.getFilter() == f, "setFilter/getFilter identity");
        l.setFilter(null);
        check(l.getFilter() == null, "clear filter");
        // null record NPE（lambda 不接收 null）
        try {
            f.isLoggable(null);
            check(false, "isLoggable(null) behavior");
        } catch (NullPointerException e) { check(true, "lambda rejects null (NPE)"); }
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
