/*
 * @test
 * @summary com.sun.management.OperatingSystemMXBean conformance: white-box structure + black-box behavior.
 * @run main OperatingSystemMXBeanTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.ManagementFactory;

public class OperatingSystemMXBeanTest {
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

    // CPU load methods return a value in [0,1] on success, a negative value when
    // unavailable, and may produce NaN on the very first observation; only reject
    // values that are implausibly above 1.0.
    static void checkCpuLoad(double load, String m) {
        check(Double.isNaN(load) || load <= 1.0, m + " (got " + load + ")");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.management.OperatingSystemMXBean");
        check(c.isInterface(), "interface");
        check(interfaces(c).contains("java.lang.management.OperatingSystemMXBean"),
            "extends java OperatingSystemMXBean");
        Set<String> m = methodNames(c);
        String[] names = {"getCommittedVirtualMemorySize","getTotalSwapSpaceSize","getFreeSwapSpaceSize",
            "getProcessCpuTime","getFreeMemorySize","getTotalMemorySize","getCpuLoad","getProcessCpuLoad",
            "getSystemCpuLoad","getFreePhysicalMemorySize","getTotalPhysicalMemorySize"};
        for (String n : names) check(m.contains(n), "method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.lang.management.OperatingSystemMXBean base = ManagementFactory.getOperatingSystemMXBean();
        check(base instanceof com.sun.management.OperatingSystemMXBean, "platform bean instanceof");
        com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) base;

        // Memory and CPU-time counters: sensible ranges.
        long committed = os.getCommittedVirtualMemorySize();
        long total = os.getTotalMemorySize();
        long free = os.getFreeMemorySize();
        check(committed >= 0, "committedVirtualMemorySize >= 0");
        check(total > 0, "totalMemorySize > 0");
        check(free >= 0, "freeMemorySize >= 0");
        check(free <= total, "freeMemorySize <= totalMemorySize");
        check(os.getProcessCpuTime() >= 0, "processCpuTime >= 0");

        // Default-method aliases for physical memory.
        long totalPhys = os.getTotalPhysicalMemorySize();
        long freePhys = os.getFreePhysicalMemorySize();
        check(totalPhys > 0, "totalPhysicalMemorySize > 0");
        check(freePhys >= 0, "freePhysicalMemorySize >= 0");
        check(freePhys <= totalPhys, "freePhysicalMemorySize <= totalPhysicalMemorySize");

        // Swap space.
        long totalSwap = os.getTotalSwapSpaceSize();
        long freeSwap = os.getFreeSwapSpaceSize();
        check(totalSwap >= 0, "totalSwapSpaceSize >= 0");
        check(freeSwap >= 0, "freeSwapSpaceSize >= 0");
        check(freeSwap <= totalSwap, "freeSwapSpaceSize <= totalSwapSpaceSize");

        // CPU load (system + process).
        checkCpuLoad(os.getSystemCpuLoad(), "systemCpuLoad in range");
        checkCpuLoad(os.getCpuLoad(), "cpuLoad in range");
        checkCpuLoad(os.getProcessCpuLoad(), "processCpuLoad in range");

        // Base java.lang.management.OperatingSystemMXBean metrics.
        check(base.getAvailableProcessors() >= 1, "availableProcessors >= 1");
        double loadAvg = base.getSystemLoadAverage();
        check(loadAvg >= -1.0, "systemLoadAverage >= -1 (or -1 if unavailable)");
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
