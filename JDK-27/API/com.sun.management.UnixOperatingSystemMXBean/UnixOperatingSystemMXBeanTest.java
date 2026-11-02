/*
 * @test
 * @summary com.sun.management.UnixOperatingSystemMXBean conformance: file descriptor counts.
 * @run main UnixOperatingSystemMXBeanTest
 */

import java.lang.reflect.*;
import java.util.*;

public class UnixOperatingSystemMXBeanTest {
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
        Class<?> c = load("com.sun.management.UnixOperatingSystemMXBean");
        check(c.isInterface(), "interface");
        check(interfaces(c).contains("com.sun.management.OperatingSystemMXBean"), "extends OperatingSystemMXBean");
        Set<String> m = methodNames(c);
        check(m.contains("getOpenFileDescriptorCount"), "getOpenFileDescriptorCount");
        check(m.contains("getMaxFileDescriptorCount"), "getMaxFileDescriptorCount");
        java.lang.management.OperatingSystemMXBean base = java.lang.management.ManagementFactory.getOperatingSystemMXBean();
        check(base instanceof com.sun.management.UnixOperatingSystemMXBean, "platform bean instanceof Unix");
        com.sun.management.UnixOperatingSystemMXBean u = (com.sun.management.UnixOperatingSystemMXBean) base;
        check(u.getOpenFileDescriptorCount() >= 0, "openFd >= 0");
        check(u.getMaxFileDescriptorCount() > 0, "maxFd > 0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
