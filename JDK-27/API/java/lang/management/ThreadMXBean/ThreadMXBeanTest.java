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


    static void expectIAE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            check(true, what + " throws IllegalArgumentException");
            for (String f : fragments) {
                check(e.getMessage() != null && e.getMessage().contains(f),
                      what + " message contains '" + f + "' (got: " + e.getMessage() + ")");
            }
        }
    }

    static void expectNPE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw NullPointerException");
        } catch (NullPointerException e) {
            check(true, what + " throws NullPointerException");
            for (String f : fragments) {
                check(e.getMessage() == null || e.getMessage().contains(f),
                      what + " message contains '" + f + "' or is null (got: " + e.getMessage() + ")");
            }
        }
    }

    static void expectUOE(Runnable op, String what, String... fragments) {
        try {
            op.run();
            check(false, what + " should throw UnsupportedOperationException");
        } catch (UnsupportedOperationException e) {
            check(true, what + " throws UnsupportedOperationException");
            for (String f : fragments) {
                check(e.getMessage() != null && e.getMessage().contains(f),
                      what + " message contains '" + f + "' (got: " + e.getMessage() + ")");
            }
        }
    }

    static void testDeepCountsAndPeakReset() {
        java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
        check(tm.getThreadCount() >= 1, "thread count >= 1");
        check(tm.getDaemonThreadCount() >= 1, "daemon threads exist (JVM service threads)");
        check(tm.getTotalStartedThreadCount() >= 1, "total started >= 1");
        check(tm.getPeakThreadCount() >= 1, "peak >= 1");
        check(tm.getPeakThreadCount() >= tm.getThreadCount(), "peak >= current before reset");
        tm.resetPeakThreadCount();
        int after = tm.getPeakThreadCount();
        check(after >= 1, "peak after reset >= 1");
        check(after <= tm.getThreadCount(),
              "peak after reset equals the live count (count may only grow afterwards)");
    }

    static void testDeepAllThreadIds() {
        java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
        long[] ids = tm.getAllThreadIds();
        check(ids.length > 0, "all thread ids non-empty");
        boolean sawCurrent = false;
        for (long id : ids) {
            check(id > 0, "every live thread id is positive");
            if (id == Thread.currentThread().getId()) {
                sawCurrent = true;
            }
        }
        check(sawCurrent, "all thread ids includes the current thread");
    }

    static void testDeepInvalidIdMatrix() {
        final java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
        expectIAE(new Runnable() { public void run() { tm.getThreadInfo(-1L); } },
                  "getThreadInfo(-1)", "Invalid thread ID", "-1");
        expectIAE(new Runnable() { public void run() { tm.getThreadInfo(0L); } },
                  "getThreadInfo(0)", "Invalid thread ID");
        expectIAE(new Runnable() { public void run() { tm.getThreadCpuTime(-1L); } },
                  "getThreadCpuTime(-1)", "Invalid thread ID");
        expectIAE(new Runnable() { public void run() { tm.getThreadUserTime(-1L); } },
                  "getThreadUserTime(-1)", "Invalid thread ID");
        check(tm.getThreadInfo(new long[0]).length == 0, "empty id array -> empty result");
        check(tm.getThreadInfo(99999999L) == null, "unknown id -> null, no exception");
    }

    static void testDeepCpuTimeLifecycle() {
        java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
        check(tm.isCurrentThreadCpuTimeSupported(), "current thread cpu time supported (HotSpot)");
        boolean original = tm.isThreadCpuTimeEnabled();
        tm.setThreadCpuTimeEnabled(true);
        check(tm.isThreadCpuTimeEnabled(), "setThreadCpuTimeEnabled(true) applied");
        check(tm.getCurrentThreadCpuTime() >= 0, "cpu time >= 0 when enabled");
        check(tm.getCurrentThreadUserTime() >= 0, "user time >= 0 when enabled");
        long id = Thread.currentThread().getId();
        check(tm.getThreadCpuTime(id) >= 0, "per-thread cpu time >= 0");
        check(tm.getThreadUserTime(id) >= 0, "per-thread user time >= 0");
        tm.setThreadCpuTimeEnabled(false);
        check(!tm.isThreadCpuTimeEnabled(), "setThreadCpuTimeEnabled(false) applied");
        check(tm.getCurrentThreadCpuTime() == -1, "cpu time is -1 while disabled");
        check(tm.getCurrentThreadUserTime() == -1, "user time is -1 while disabled");
        tm.setThreadCpuTimeEnabled(original);
        check(tm.isThreadCpuTimeEnabled() == original, "cpu time flag restored");
    }

    static void testDeepContentionMonitoring() {
        java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
        check(tm.isThreadContentionMonitoringSupported(), "contention monitoring supported (HotSpot)");
        boolean original = tm.isThreadContentionMonitoringEnabled();
        tm.setThreadContentionMonitoringEnabled(true);
        check(tm.isThreadContentionMonitoringEnabled(), "contention monitoring enabled");
        tm.setThreadContentionMonitoringEnabled(false);
        check(!tm.isThreadContentionMonitoringEnabled(), "contention monitoring disabled");
        tm.setThreadContentionMonitoringEnabled(original);
        check(tm.isThreadContentionMonitoringEnabled() == original, "contention flag restored");
    }

    static void testDeepDeadlockProbes() {
        java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
        long[] d = tm.findDeadlockedThreads();
        check(d == null || d.length == 0, "no deadlocked threads: null or empty, got " + java.util.Arrays.toString(d));
        long[] m = tm.findMonitorDeadlockedThreads();
        check(m == null || m.length == 0, "no monitor-deadlocked threads: " + java.util.Arrays.toString(m));
    }

    static void testDeepSupportFlagsAndDump() {
        java.lang.management.ThreadMXBean tm = java.lang.management.ManagementFactory.getThreadMXBean();
        check(tm.isObjectMonitorUsageSupported(), "object monitor usage supported");
        check(tm.isSynchronizerUsageSupported(), "synchronizer usage supported");
        java.lang.management.ThreadInfo[] all = tm.dumpAllThreads(false, false);
        check(all.length >= 1, "dumpAllThreads(false,false) non-empty");
    }

    static void testDeepSingletonIdentity() {
        check(java.lang.management.ManagementFactory.getThreadMXBean()
                == java.lang.management.ManagementFactory.getThreadMXBean(),
              "getThreadMXBean() returns the same instance on every call");
        check(java.lang.management.ManagementFactory.getPlatformMXBean(java.lang.management.ThreadMXBean.class)
                == java.lang.management.ManagementFactory.getThreadMXBean(),
              "getPlatformMXBean(ThreadMXBean.class) is the same singleton");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepCountsAndPeakReset();
        testDeepAllThreadIds();
        testDeepInvalidIdMatrix();
        testDeepCpuTimeLifecycle();
        testDeepContentionMonitoring();
        testDeepDeadlockProbes();
        testDeepSupportFlagsAndDump();
        testDeepSingletonIdentity();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
