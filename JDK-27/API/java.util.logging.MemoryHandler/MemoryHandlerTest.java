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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
