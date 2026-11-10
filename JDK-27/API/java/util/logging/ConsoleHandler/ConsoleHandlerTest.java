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


    // ===================== 契约深化：发布到 System.err（重定向捕获）/默认 INFO+SimpleFormatter/级别门控 =====================
    static void testContracts() {
        java.io.PrintStream oldErr = System.err;
        java.io.ByteArrayOutputStream cap = new java.io.ByteArrayOutputStream();
        System.setErr(new java.io.PrintStream(cap, true));
        try {
            java.util.logging.ConsoleHandler ch = new java.util.logging.ConsoleHandler();
            check(ch.getLevel() == java.util.logging.Level.INFO, "default level INFO");
            check(ch.getFormatter() instanceof java.util.logging.SimpleFormatter, "default SimpleFormatter");
            ch.publish(new java.util.logging.LogRecord(java.util.logging.Level.INFO, "to-stderr"));
            ch.flush();
            check(cap.toString().contains("to-stderr"), "publishes to System.err (captured)");
            // 低于级别：不写
            cap.reset();
            ch.publish(new java.util.logging.LogRecord(java.util.logging.Level.FINE, "dropped-fine"));
            ch.flush();
            check(!cap.toString().contains("dropped-fine"), "FINE dropped at default INFO");
            // 提升 level 后 FINE 通过
            ch.setLevel(java.util.logging.Level.ALL);
            ch.publish(new java.util.logging.LogRecord(java.util.logging.Level.FINE, "kept-fine"));
            ch.flush();
            check(cap.toString().contains("kept-fine"), "FINE passes after setLevel(ALL)");
            ch.close();
        } finally {
            System.setErr(oldErr);
        }

        // 默认 encoding 为 null；setLevel(null)/setFormatter(null) NPE（不写任何输出）
        java.util.logging.ConsoleHandler enc = new java.util.logging.ConsoleHandler();
        check(enc.getEncoding() == null, "default encoding null");
        try {
            enc.setLevel(null);
            check(false, "setLevel(null) NPE");
        } catch (NullPointerException e) { check(true, "setLevel(null) NPE"); }
        try {
            enc.setFormatter(null);
            check(false, "setFormatter(null) NPE");
        } catch (NullPointerException e) { check(true, "setFormatter(null) NPE"); }
        // close 幂等（重定向 err，避免关闭真实 System.err）
        java.io.PrintStream oldErr2 = System.err;
        System.setErr(new java.io.PrintStream(new java.io.ByteArrayOutputStream(), true));
        try {
            java.util.logging.ConsoleHandler idem = new java.util.logging.ConsoleHandler();
            idem.close();
            idem.close();
            check(true, "close idempotent");
        } finally {
            System.setErr(oldErr2);
        }
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
