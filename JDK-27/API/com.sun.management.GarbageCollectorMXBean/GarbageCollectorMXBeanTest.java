/*
 * @test
 * @summary com.sun.management.GarbageCollectorMXBean conformance: platform MXBean interface.
 * @run main GarbageCollectorMXBeanTest
 */

import java.lang.reflect.*;
import java.util.*;

public class GarbageCollectorMXBeanTest {
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
        Class<?> c = load("com.sun.management.GarbageCollectorMXBean");
        check(c.isInterface(), "interface");
        check(interfaces(c).contains("java.lang.management.GarbageCollectorMXBean"), "extends java GC MXBean");
        check(methodNames(c).contains("getLastGcInfo"), "getLastGcInfo");
        boolean any = false;
        for (java.lang.management.GarbageCollectorMXBean b : java.lang.management.ManagementFactory.getGarbageCollectorMXBeans()) if (b instanceof com.sun.management.GarbageCollectorMXBean) any = true;
        check(any, "platform GC beans implement com.sun GCMXBean");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
