/*
 * @test
 * @summary com.sun.management.GcInfo conformance: hierarchy and declared members via reflection.
 * @run main GcInfoTest
 */

public class GcInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.management.GcInfo", false, null);
        check(c.getName().equals("com.sun.management.GcInfo"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.management.openmbean.CompositeData"), "implements javax.management.openmbean.CompositeData");
        check(in.contains("javax.management.openmbean.CompositeDataView"), "implements javax.management.openmbean.CompositeDataView");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("containsKey"), "declares containsKey");
        check(ms.contains("containsValue"), "declares containsValue");
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("from"), "declares from");
        check(ms.contains("get"), "declares get");
        check(ms.contains("getAll"), "declares getAll");
        check(ms.contains("getCompositeType"), "declares getCompositeType");
        check(ms.contains("getDuration"), "declares getDuration");
        check(ms.contains("getEndTime"), "declares getEndTime");
        check(ms.contains("getId"), "declares getId");
        check(ms.contains("getMemoryUsageAfterGc"), "declares getMemoryUsageAfterGc");
        check(ms.contains("getMemoryUsageBeforeGc"), "declares getMemoryUsageBeforeGc");
        check(ms.contains("getStartTime"), "declares getStartTime");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("toCompositeData"), "declares toCompositeData");
        check(ms.contains("toString"), "declares toString");
        check(ms.contains("values"), "declares values");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
