/*
 * @test
 * @summary com.sun.management.GcInfo conformance: composite-data view shape.
 * @run main GcInfoTest
 */

import java.lang.reflect.*;
import java.util.*;

public class GcInfoTest {
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
        Class<?> c = load("com.sun.management.GcInfo");
        check(!c.isInterface(), "not an interface");
        check(interfaces(c).contains("javax.management.openmbean.CompositeData"), "CompositeData");
        check(interfaces(c).contains("javax.management.openmbean.CompositeDataView"), "CompositeDataView");
        Set<String> m = methodNames(c);
        String[] names = {"getId","getStartTime","getEndTime","getDuration","getMemoryUsageBeforeGc","getMemoryUsageAfterGc","from","toCompositeData"};
        for (String n : names) check(m.contains(n), "method " + n);
        System.gc();
        com.sun.management.GcInfo gi = null;
        for (java.lang.management.GarbageCollectorMXBean b : java.lang.management.ManagementFactory.getGarbageCollectorMXBeans())
            if (b instanceof com.sun.management.GarbageCollectorMXBean) { com.sun.management.GcInfo x = ((com.sun.management.GarbageCollectorMXBean)b).getLastGcInfo(); if (x != null) gi = x; }
        if (gi != null) { check(gi.getDuration() >= 0, "duration >= 0"); check(gi.getId() >= 0, "id >= 0"); }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
