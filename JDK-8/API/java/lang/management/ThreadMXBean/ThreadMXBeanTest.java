/*
 * @test
 * @summary java.lang.management.ThreadMXBean conformance: thread counts and ids.
 * @run main ThreadMXBeanTest
 */

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class ThreadMXBeanTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testThreadMXBean() {
        ThreadMXBean tmx = ManagementFactory.getThreadMXBean();
        check(tmx.getThreadCount() > 0, "getThreadCount");
        check(tmx.getPeakThreadCount() > 0, "getPeakThreadCount");
        check(tmx.getTotalStartedThreadCount() > 0, "getTotalStartedThreadCount");
        check(tmx.getDaemonThreadCount() > 0, "getDaemonThreadCount");
        check(tmx.getAllThreadIds().length > 0, "getAllThreadIds");
        check(tmx.getObjectName() != null, "getObjectName");
        if (tmx.isCurrentThreadCpuTimeSupported()) {
            check(tmx.getCurrentThreadCpuTime() >= 0, "getCurrentThreadCpuTime");
        }
    }

    public static void main(String[] args) {
        testThreadMXBean();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
