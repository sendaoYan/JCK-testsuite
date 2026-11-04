/*
 * @test
 * @summary com.sun.management.UnixOperatingSystemMXBean conformance: white-box structure + black-box behavior.
 * @run main UnixOperatingSystemMXBeanTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.lang.management.ManagementFactory;

public class UnixOperatingSystemMXBeanTest {
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
        Class<?> c = load("com.sun.management.UnixOperatingSystemMXBean");
        check(c.isInterface(), "interface");
        check(interfaces(c).contains("com.sun.management.OperatingSystemMXBean"),
            "extends OperatingSystemMXBean");
        Set<String> m = methodNames(c);
        check(m.contains("getOpenFileDescriptorCount"), "getOpenFileDescriptorCount");
        check(m.contains("getMaxFileDescriptorCount"), "getMaxFileDescriptorCount");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        java.lang.management.OperatingSystemMXBean base = ManagementFactory.getOperatingSystemMXBean();
        if (!(base instanceof com.sun.management.UnixOperatingSystemMXBean)) {
            System.out.println("NOTE: not a UnixOperatingSystemMXBean; skipping Unix-specific checks.");
            return;
        }
        com.sun.management.UnixOperatingSystemMXBean u = (com.sun.management.UnixOperatingSystemMXBean) base;

        // File descriptor counts.
        long open = u.getOpenFileDescriptorCount();
        long max = u.getMaxFileDescriptorCount();
        check(open >= 0, "openFileDescriptorCount >= 0");
        check(max > 0, "maxFileDescriptorCount > 0");
        check(open <= max, "openFileDescriptorCount <= maxFileDescriptorCount");

        // Inherited OperatingSystemMXBean metrics remain callable through the Unix bean.
        check(u.getCommittedVirtualMemorySize() >= 0, "committedVirtualMemorySize >= 0");
        check(u.getTotalMemorySize() > 0, "totalMemorySize > 0");
        check(u.getFreeMemorySize() >= 0, "freeMemorySize >= 0");
        check(u.getProcessCpuTime() >= 0, "processCpuTime >= 0");
        check(u.getTotalSwapSpaceSize() >= 0, "totalSwapSpaceSize >= 0");
        check(u.getFreeSwapSpaceSize() >= 0, "freeSwapSpaceSize >= 0");
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
