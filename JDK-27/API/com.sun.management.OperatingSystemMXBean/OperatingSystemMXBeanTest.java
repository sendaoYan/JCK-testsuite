/*
 * @test
 * @summary com.sun.management.OperatingSystemMXBean conformance: OS metrics getters.
 * @run main OperatingSystemMXBeanTest
 */

import java.lang.reflect.*;
import java.util.*;

public class OperatingSystemMXBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    public static void main(String[] a) throws Exception {
        Class<?> c = load("com.sun.management.OperatingSystemMXBean");
        check(c.isInterface(), "interface");
        Set<String> m = methodNames(c);
        String[] names = {"getCommittedVirtualMemorySize","getTotalSwapSpaceSize","getFreeSwapSpaceSize","getProcessCpuTime","getFreeMemorySize","getTotalMemorySize","getCpuLoad","getProcessCpuLoad"};
        for (String n : names) check(m.contains(n), "method " + n);
        java.lang.management.OperatingSystemMXBean base = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        check(base instanceof com.sun.management.OperatingSystemMXBean, "platform bean instanceof");
        com.sun.management.OperatingSystemMXBean os = (com.sun.management.OperatingSystemMXBean) base;
        check(os.getFreeMemorySize() >= 0, "freeMemory >= 0");
        check(os.getTotalMemorySize() > 0, "totalMemory > 0");
        check(os.getCommittedVirtualMemorySize() > 0, "committedVirt > 0");
        check(os.getProcessCpuTime() >= 0, "processCpuTime >= 0");
        check(os.getFreeSwapSpaceSize() >= 0, "freeSwap >= 0");
        check(os.getTotalSwapSpaceSize() >= 0, "totalSwap >= 0");
        double load = os.getCpuLoad();
        check(load >= -1.0 && load <= 1.0, "cpuLoad in range");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
