/*
 * @test
 * @summary java.util.logging.MemoryHandler conformance: white-box structure + black-box behavior.
 * @run main MemoryHandlerTest
 */

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.MemoryHandler;

public class MemoryHandlerTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.util.logging.MemoryHandler");
        check(Modifier.isPublic(c.getModifiers()), "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(Handler.class.isAssignableFrom(c), "extends Handler");
        check(c.getSuperclass() == Handler.class, "direct superclass Handler");
        check(c.getConstructor() != null, "no-arg constructor");
        check(c.getConstructor(Handler.class, int.class, Level.class) != null,
                "(Handler,int,Level) constructor");
        check(c.getMethod("push") != null, "push()");
        check(c.getMethod("getPushLevel") != null, "getPushLevel()");
        check(c.getMethod("setPushLevel", Level.class) != null, "setPushLevel(Level)");
        check(c.getMethod("flush") != null, "flush()");
        check(c.getMethod("close") != null, "close()");
        check(c.getMethod("isLoggable", LogRecord.class) != null, "isLoggable(LogRecord)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        CapturingHandler target = new CapturingHandler();

        // constructor validation
        expectThrows(NullPointerException.class,
                () -> new MemoryHandler(null, 5, Level.SEVERE), "null target NPE");
        expectThrows(NullPointerException.class,
                () -> new MemoryHandler(target, 5, null), "null pushLevel NPE");
        expectThrows(IllegalArgumentException.class,
                () -> new MemoryHandler(target, 0, Level.SEVERE), "size <= 0 IAE");

        MemoryHandler mh = new MemoryHandler(target, 5, Level.SEVERE);
        check(mh.getPushLevel() == Level.SEVERE, "initial pushLevel SEVERE");
        check(mh.getLevel() == Level.ALL, "default level ALL");

        // push level round-trip + null rejection
        mh.setPushLevel(Level.WARNING);
        check(mh.getPushLevel() == Level.WARNING, "set/getPushLevel");
        expectThrows(NullPointerException.class, () -> mh.setPushLevel(null), "setPushLevel(null) NPE");
        mh.setPushLevel(Level.SEVERE);

        // below-push-level records are buffered, not pushed
        mh.publish(new LogRecord(Level.INFO, "buffered-1"));
        mh.publish(new LogRecord(Level.WARNING, "buffered-2"));
        check(target.records.size() == 0, "below-push records buffered (not pushed)");

        // flush does NOT push
        mh.flush();
        check(target.records.size() == 0, "flush does not push buffer");

        // push-level record pushes the whole buffer
        mh.publish(new LogRecord(Level.SEVERE, "trigger"));
        check(target.records.size() == 3, "push writes buffered + trigger");
        check("buffered-1".equals(target.records.get(0).getMessage()), "push order: buffered-1 first");
        check("buffered-2".equals(target.records.get(1).getMessage()), "push order: buffered-2 second");
        check("trigger".equals(target.records.get(2).getMessage()), "push order: trigger last");

        // explicit push() of a fresh buffer
        mh.publish(new LogRecord(Level.INFO, "explicit-1"));
        mh.publish(new LogRecord(Level.INFO, "explicit-2"));
        mh.push();
        check(target.records.size() == 5, "explicit push drains buffer");
        check("explicit-1".equals(target.records.get(3).getMessage()), "explicit push first");
        check("explicit-2".equals(target.records.get(4).getMessage()), "explicit push second");

        // close() closes target and turns this handler OFF
        mh.close();
        check(target.closed, "close closes target");
        check(mh.getLevel() == Level.OFF, "close sets level OFF");
    }

    static final class CapturingHandler extends Handler {
        final List<LogRecord> records = new ArrayList<>();
        boolean closed;
        public void publish(LogRecord record) { records.add(record); }
        public void flush() { }
        public void close() { closed = true; }
    }


    // ===================== 契约深化：缓冲 FIFO/pushLevel 触发整体下发/手动 push/级别过滤/驱逐（丢最旧） =====================
    static void testContracts() throws Exception {
        final java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.util.logging.StreamHandler target = new java.util.logging.StreamHandler(bos, new java.util.logging.SimpleFormatter());
        java.util.logging.MemoryHandler mh = new java.util.logging.MemoryHandler(target, 3, java.util.logging.Level.SEVERE);
        check(mh.getPushLevel() == java.util.logging.Level.SEVERE, "pushLevel from ctor");
        java.util.logging.Logger l = java.util.logging.Logger.getLogger("jck.memory.fifo");
        l.setUseParentHandlers(false);
        l.addHandler(mh);
        l.setLevel(java.util.logging.Level.ALL);
        mh.setLevel(java.util.logging.Level.ALL);
        // 低于 pushLevel：仅缓冲（目标 handler 内部也缓冲，flush 前无字节）
        l.info("one");
        l.info("two");
        target.flush();
        check(bos.size() == 0, "below pushLevel only buffers");
        // 达到 pushLevel：缓冲记录 + 触发记录一并下发
        l.severe("boom");
        target.flush();
        String out = bos.toString();
        check(out.contains("one") && out.contains("two") && out.contains("boom"),
              "trigger flushes buffered FIFO plus trigger record");
        // 手动 push()
        bos.reset();
        l.info("three");
        check(bos.size() == 0, "manual push pending");
        mh.push();
        target.flush();
        check(bos.toString().contains("three"), "push() flushes buffer to target");
        // 容量驱逐：缓冲 3，写 5 条再触发 → 最旧被丢弃
        bos.reset();
        for (int i = 1; i <= 5; i++) l.info("evict-" + i);
        l.severe("boom2");
        target.flush();
        String ev = bos.toString();
        check(!ev.contains("evict-1"), "oldest evicted beyond capacity");
        check(ev.contains("evict-5") && ev.contains("boom2"), "newest plus trigger survive eviction");
        // 级别过滤：低于 handler 级别的记录丢弃
        java.util.logging.MemoryHandler strict = new java.util.logging.MemoryHandler(target, 2, java.util.logging.Level.SEVERE);
        strict.setLevel(java.util.logging.Level.WARNING);
        check(!strict.isLoggable(new java.util.logging.LogRecord(java.util.logging.Level.INFO, "x")),
              "isLoggable false below level");
        check(strict.isLoggable(new java.util.logging.LogRecord(java.util.logging.Level.WARNING, "x")),
              "isLoggable true at level");
        // null target NPE
        try {
            new java.util.logging.MemoryHandler(null, 10, java.util.logging.Level.SEVERE);
            check(false, "MemoryHandler(null target) NPE");
        } catch (NullPointerException e) { check(true, "MemoryHandler(null target) NPE"); }
        // 非法容量 IAE
        try {
            new java.util.logging.MemoryHandler(target, 0, java.util.logging.Level.SEVERE);
            check(false, "size 0 IAE");
        } catch (IllegalArgumentException e) { check(true, "size 0 IAE"); }
        l.removeHandler(mh);
    }

    public static void main(String[] args) throws Exception {
        try { testContracts(); } catch (Exception e) { throw new RuntimeException(e); }
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
