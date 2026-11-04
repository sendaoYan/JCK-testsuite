/*
 * @test
 * @summary com.sun.management.GarbageCollectionNotificationInfo conformance: white-box structure + black-box behavior.
 * @run main GarbageCollectionNotificationInfoTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.openmbean.CompositeData;

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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.management.GarbageCollectionNotificationInfo");
        check(!c.isInterface(), "not an interface");
        check(interfaces(c).contains("javax.management.openmbean.CompositeDataView"), "CompositeDataView");
        Set<String> m = methodNames(c);
        for (String n : new String[] {"getGcName","getGcAction","getGcCause","getGcInfo","from","toCompositeData"})
            check(m.contains(n), "method " + n);
        check(fieldNames(c).contains("GARBAGE_COLLECTION_NOTIFICATION"), "constant field");
        check("com.sun.management.gc.notification".equals(
            com.sun.management.GarbageCollectionNotificationInfo.GARBAGE_COLLECTION_NOTIFICATION),
            "notification type value");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // Constructor + getters (defensive null-GcInfo path).
        com.sun.management.GarbageCollectionNotificationInfo n =
            new com.sun.management.GarbageCollectionNotificationInfo("young", "end of minor GC", "System.gc()", null);
        check(n.getGcName().equals("young"), "gcName");
        check(n.getGcAction().equals("end of minor GC"), "gcAction");
        check(n.getGcCause().equals("System.gc()"), "gcCause");
        check(n.getGcInfo() == null, "gcInfo null when constructed with null");

        // Constructor rejects null gcName/gcAction/gcCause with NPE.
        checkThrowsNpe(new Runnable() {
            public void run() { new com.sun.management.GarbageCollectionNotificationInfo(null, "a", "c", null); }
        }, "NPE on null gcName");
        checkThrowsNpe(new Runnable() {
            public void run() { new com.sun.management.GarbageCollectionNotificationInfo("n", null, "c", null); }
        }, "NPE on null gcAction");
        checkThrowsNpe(new Runnable() {
            public void run() { new com.sun.management.GarbageCollectionNotificationInfo("n", "a", null, null); }
        }, "NPE on null gcCause");

        // CompositeDataView round-trip (toCompositeData does not materialize eagerly,
        // so the null-GcInfo object can still be converted and read back).
        CompositeData cd = n.toCompositeData(null);
        check(cd != null, "toCompositeData non-null");
        com.sun.management.GarbageCollectionNotificationInfo back =
            com.sun.management.GarbageCollectionNotificationInfo.from(cd);
        check(back != null, "from non-null");
        check(back.getGcName().equals("young"), "round-trip gcName");
        check(back.getGcAction().equals("end of minor GC"), "round-trip gcAction");
        check(back.getGcCause().equals("System.gc()"), "round-trip gcCause");
        check(back.getGcInfo() == null, "round-trip gcInfo null");

        // from(null) returns null.
        check(com.sun.management.GarbageCollectionNotificationInfo.from(null) == null,
            "from(null) == null");
    }

    static void checkThrowsNpe(Runnable r, String m) {
        try {
            r.run();
            throw new AssertionError(m + ": expected NullPointerException");
        } catch (NullPointerException e) {
            check(true, m);
        }
    }

    public static void main(String[] a) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
