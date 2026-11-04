/*
 * @test
 * @summary java.lang.management.ThreadMXBean conformance: white-box structure + black-box behavior.
 * @run main ThreadMXBeanTest
 */
import java.lang.management.*;
import java.util.Arrays;

public class ThreadMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.management.ThreadMXBean");
        check(c.isInterface(), "interface");
        check(PlatformManagedObject.class.isAssignableFrom(c), "extends PlatformManagedObject");
        check(c.getMethod("getThreadCount").getReturnType() == int.class, "getThreadCount returns int");
        check(c.getMethod("getPeakThreadCount").getReturnType() == int.class, "getPeakThreadCount returns int");
        check(c.getMethod("getTotalStartedThreadCount").getReturnType() == long.class,
              "getTotalStartedThreadCount returns long");
        check(c.getMethod("getDaemonThreadCount").getReturnType() == int.class, "getDaemonThreadCount returns int");
        check(c.getMethod("getAllThreadIds").getReturnType() == long[].class, "getAllThreadIds returns long[]");
        check(c.getMethod("getThreadInfo", long.class).getReturnType() == ThreadInfo.class,
              "getThreadInfo(long) returns ThreadInfo");
        check(c.getMethod("findDeadlockedThreads").getReturnType() == long[].class, "findDeadlockedThreads returns long[]");
        check(c.getMethod("dumpAllThreads", boolean.class, boolean.class).getReturnType() == ThreadInfo[].class,
              "dumpAllThreads returns ThreadInfo[]");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        ThreadMXBean b = ManagementFactory.getThreadMXBean();
        check(b != null, "non-null");
        int count = b.getThreadCount();
        check(count > 0, "thread count > 0");
        check(b.getPeakThreadCount() >= count, "peak >= count");
        check(b.getTotalStartedThreadCount() >= count, "totalStarted >= count");
        check(b.getDaemonThreadCount() >= 0, "daemon count >= 0");

        long[] ids = b.getAllThreadIds();
        check(ids != null && ids.length == count, "allThreadIds length == thread count");
        Arrays.sort(ids);
        check(Arrays.binarySearch(ids, Thread.currentThread().getId()) >= 0, "current thread id present");

        ThreadInfo info = b.getThreadInfo(Thread.currentThread().getId());
        check(info != null && info.getThreadId() == Thread.currentThread().getId(), "getThreadInfo current");

        check(b.getCurrentThreadCpuTime() >= -1, "current cpu time >= -1");
        check(b.getCurrentThreadUserTime() >= -1, "current user time >= -1");
        check(b.findDeadlockedThreads() == null, "no deadlocked threads");
        check(b.findMonitorDeadlockedThreads() == null, "no monitor-deadlocked threads");
        check(b.getObjectName() != null, "objectName non-null");
        check(b.dumpAllThreads(false, false) != null, "dumpAllThreads non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
