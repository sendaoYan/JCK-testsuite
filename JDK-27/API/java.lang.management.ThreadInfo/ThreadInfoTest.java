/*
 * @test
 * @summary java.lang.management.ThreadInfo conformance: white-box structure + black-box behavior.
 * @run main ThreadInfoTest
 */
import java.lang.management.*;

public class ThreadInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.ThreadInfo");
        check(!c.isInterface() && !java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getMethod("getThreadId").getReturnType() == long.class, "getThreadId returns long");
        check(c.getMethod("getThreadName").getReturnType() == String.class, "getThreadName returns String");
        check(c.getMethod("getThreadState").getReturnType() == Thread.State.class, "getThreadState returns Thread.State");
        check(c.getMethod("getBlockedCount").getReturnType() == long.class, "getBlockedCount returns long");
        check(c.getMethod("getWaitedCount").getReturnType() == long.class, "getWaitedCount returns long");
        check(c.getMethod("getStackTrace").getReturnType() == StackTraceElement[].class, "getStackTrace returns StackTraceElement[]");
        check(c.getMethod("isDaemon").getReturnType() == boolean.class, "isDaemon returns boolean");
        check(c.getMethod("getPriority").getReturnType() == int.class, "getPriority returns int");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ThreadMXBean tm = ManagementFactory.getThreadMXBean();
        long id = Thread.currentThread().getId();
        ThreadInfo info = tm.getThreadInfo(id);
        check(info != null, "getThreadInfo(current) non-null");
        check(info.getThreadId() == id, "getThreadId matches current");
        check(Thread.currentThread().getName().equals(info.getThreadName()), "getThreadName matches");
        check(info.getThreadState() != null, "thread state non-null");
        check(info.getPriority() == Thread.currentThread().getPriority(), "getPriority matches");
        check(info.isDaemon() == Thread.currentThread().isDaemon(), "isDaemon matches");
        check(!info.isSuspended(), "isSuspended false");
        check(info.getBlockedCount() >= 0, "blockedCount non-negative");
        check(info.getWaitedCount() >= 0, "waitedCount non-negative");
        check(info.getBlockedTime() >= -1, "blockedTime >= -1 (contention monitoring may be off)");
        check(info.getWaitedTime() >= -1, "waitedTime >= -1 (contention monitoring may be off)");
        check(info.toString() != null && !info.toString().isEmpty(), "toString non-empty");

        // the single-arg getThreadInfo does not capture a stack trace; use maxDepth for that
        ThreadInfo full = tm.getThreadInfo(id, Integer.MAX_VALUE);
        StackTraceElement[] st = full.getStackTrace();
        check(st != null && st.length > 0, "stack trace non-empty with maxDepth");

        ThreadInfo[] infos = tm.getThreadInfo(new long[]{id});
        check(infos != null && infos.length == 1 && infos[0].getThreadId() == id, "getThreadInfo(long[]) roundtrip");

        ThreadInfo limited = tm.getThreadInfo(id, 2);
        check(limited != null && limited.getThreadId() == id, "getThreadInfo(id,maxDepth) non-null");
        check(limited.getStackTrace().length == 2, "getThreadInfo(id,2) captures 2 frames");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
