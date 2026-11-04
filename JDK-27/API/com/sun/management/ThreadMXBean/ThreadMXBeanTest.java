/*
 * @test
 * @summary com.sun.management.ThreadMXBean conformance: white-box structure + black-box behavior.
 * @run main ThreadMXBeanTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.ManagementFactory;

public class ThreadMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.management.ThreadMXBean");
        check(c.isInterface(), "interface");
        check(interfaces(c).contains("java.lang.management.ThreadMXBean"), "extends java ThreadMXBean");
        Set<String> m = methodNames(c);
        String[] names = {"getThreadCpuTime","getThreadUserTime","getThreadAllocatedBytes",
            "getTotalThreadAllocatedBytes","getCurrentThreadAllocatedBytes",
            "isThreadAllocatedMemorySupported","isThreadAllocatedMemoryEnabled",
            "setThreadAllocatedMemoryEnabled"};
        for (String n : names) check(m.contains(n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.lang.management.ThreadMXBean base = ManagementFactory.getThreadMXBean();
        check(base instanceof com.sun.management.ThreadMXBean, "platform bean instanceof");
        com.sun.management.ThreadMXBean t = (com.sun.management.ThreadMXBean) base;
        long curId = Thread.currentThread().threadId();

        // CPU time: enable if supported, restore in finally.
        boolean cpuSupported = t.isThreadCpuTimeSupported();
        boolean cpuEnabled = t.isThreadCpuTimeEnabled();
        if (cpuSupported && !cpuEnabled) t.setThreadCpuTimeEnabled(true);
        try {
            long curCpu = t.getCurrentThreadCpuTime();
            long curUser = t.getCurrentThreadUserTime();
            long tidCpu = t.getThreadCpuTime(curId);
            long tidUser = t.getThreadUserTime(curId);
            if (cpuSupported) {
                check(curCpu >= 0, "currentThreadCpuTime >= 0");
                check(curUser >= 0, "currentThreadUserTime >= 0");
                check(tidCpu >= 0, "threadCpuTime(current) >= 0");
                check(tidUser >= 0, "threadUserTime(current) >= 0");
            } else {
                check(curCpu == -1 && tidCpu == -1, "cpu time -1 when unsupported");
            }

            // Array variant returns one entry per id; -1 for dead/disabled threads.
            long[] ids = base.getAllThreadIds();
            check(ids != null && ids.length > 0, "allThreadIds non-empty");
            long[] cpuArr = t.getThreadCpuTime(ids);
            check(cpuArr != null && cpuArr.length == ids.length, "threadCpuTime array length");
            long[] userArr = t.getThreadUserTime(ids);
            check(userArr != null && userArr.length == ids.length, "threadUserTime array length");
            for (long v : cpuArr) check(v >= -1, "cpuTime array element >= -1");
            for (long v : userArr) check(v >= -1, "userTime array element >= -1");
        } finally {
            if (cpuSupported && !cpuEnabled) t.setThreadCpuTimeEnabled(false);
        }

        // Allocated memory: enable if supported, restore in finally.
        boolean allocSupported = t.isThreadAllocatedMemorySupported();
        boolean allocEnabled = false;
        if (allocSupported) {
            allocEnabled = t.isThreadAllocatedMemoryEnabled();
            if (!allocEnabled) t.setThreadAllocatedMemoryEnabled(true);
        }
        try {
            long alloc = t.getThreadAllocatedBytes(curId);
            long curAlloc = t.getCurrentThreadAllocatedBytes();
            long totalAlloc = t.getTotalThreadAllocatedBytes();
            if (allocSupported) {
                check(alloc >= 0, "threadAllocatedBytes(current) >= 0");
                check(curAlloc >= 0, "currentThreadAllocatedBytes >= 0");
                check(totalAlloc >= 0, "totalThreadAllocatedBytes >= 0");

                // Allocation is monotonic for the current thread.
                long before = t.getThreadAllocatedBytes(curId);
                byte[] bump = new byte[4096];
                long after = t.getThreadAllocatedBytes(curId);
                check(after >= before, "allocated bytes non-decreasing after allocation");
                // silence the compiler warning for the intentionally unused buffer
                if (bump.length != 4096) throw new AssertionError("unreachable");
            } else {
                check(alloc == -1, "threadAllocatedBytes -1 when unsupported");
            }

            // Array variant returns one entry per id.
            long[] ids = base.getAllThreadIds();
            long[] allocArr = t.getThreadAllocatedBytes(ids);
            check(allocArr != null && allocArr.length == ids.length, "threadAllocatedBytes array length");
            for (long v : allocArr) check(v >= -1, "allocatedBytes array element >= -1");
        } finally {
            if (allocSupported && !allocEnabled) t.setThreadAllocatedMemoryEnabled(false);
        }
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
