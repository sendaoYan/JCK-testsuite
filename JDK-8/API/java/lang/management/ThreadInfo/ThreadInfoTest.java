/*
 * @test
 * @summary java.lang.management.ThreadInfo conformance: via ThreadMXBean.
 * @run main ThreadInfoTest
 */

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

public class ThreadInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThreadInfo() {
        ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
        long[] ids = tmx.getAllThreadIds();
        check(ids.length > 0, "has thread ids");
        ThreadInfo ti = tmx.getThreadInfo(ids[0]);
        check(ti != null, "getThreadInfo");
        check(ti.getThreadId() == ids[0], "getThreadId");
        check(ti.getThreadName() != null, "getThreadName");
        check(ti.getThreadState() != null, "getThreadState");
        check(ti.getBlockedCount() >= 0, "getBlockedCount");
        check(ti.getWaitedCount() >= 0, "getWaitedCount");
        check(ti.isInNative() == ti.isInNative(), "isInNative callable");
        check(ti.toString() != null, "toString");
    }

    public static void main(String[] args) {
        testThreadInfo();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
