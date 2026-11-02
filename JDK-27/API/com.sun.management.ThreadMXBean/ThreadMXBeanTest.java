/*
 * @test
 * @summary com.sun.management.ThreadMXBean conformance: thread allocation metrics.
 * @run main ThreadMXBeanTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ThreadMXBeanTest {
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
        Class<?> c = load("com.sun.management.ThreadMXBean");
        check(c.isInterface(), "interface");
        Set<String> m = methodNames(c);
        String[] names = {"getThreadCpuTime","getThreadUserTime","getThreadAllocatedBytes","isThreadAllocatedMemorySupported","isThreadAllocatedMemoryEnabled","setThreadAllocatedMemoryEnabled"};
        for (String n : names) check(m.contains(n), "method " + n);
        java.lang.management.ThreadMXBean base = java.lang.management.ManagementFactory.getThreadMXBean();
        check(base instanceof com.sun.management.ThreadMXBean, "platform bean instanceof");
        com.sun.management.ThreadMXBean t = (com.sun.management.ThreadMXBean) base;
        t.isThreadAllocatedMemorySupported();
        long[] ids = base.getAllThreadIds();
        long[] times = t.getThreadCpuTime(ids);
        check(times != null && times.length == ids.length, "getThreadCpuTime array length");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
