/*
 * @test
 * @summary com.sun.management.GarbageCollectionNotificationInfo conformance: constructor and getters.
 * @run main GarbageCollectionNotificationInfoTest
 */

import java.lang.reflect.*;
import java.util.*;

public class GarbageCollectionNotificationInfoTest {
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
        Class<?> c = load("com.sun.management.GarbageCollectionNotificationInfo");
        check(!c.isInterface(), "not an interface");
        check(interfaces(c).contains("javax.management.openmbean.CompositeDataView"), "CompositeDataView");
        com.sun.management.GarbageCollectionNotificationInfo n = new com.sun.management.GarbageCollectionNotificationInfo("young", "end of minor GC", "System.gc()", null);
        check(n.getGcName().equals("young"), "gcName");
        check(n.getGcAction().equals("end of minor GC"), "gcAction");
        check(n.getGcCause().equals("System.gc()"), "gcCause");
        check(n.getGcInfo() == null, "gcInfo null");
        check(com.sun.management.GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION != null, "constant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
