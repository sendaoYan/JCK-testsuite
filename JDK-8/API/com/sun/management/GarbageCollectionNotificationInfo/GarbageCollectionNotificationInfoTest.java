/*
 * @test
 * @summary com.sun.management.GarbageCollectionNotificationInfo conformance: hierarchy and declared members via reflection.
 * @run main GarbageCollectionNotificationInfoTest
 */

public class GarbageCollectionNotificationInfoTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.management.GarbageCollectionNotificationInfo", false, null);
        check(c.getName().equals("com.sun.management.GarbageCollectionNotificationInfo"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.management.openmbean.CompositeDataView"), "implements javax.management.openmbean.CompositeDataView");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("from"), "declares from");
        check(ms.contains("getGcAction"), "declares getGcAction");
        check(ms.contains("getGcCause"), "declares getGcCause");
        check(ms.contains("getGcInfo"), "declares getGcInfo");
        check(ms.contains("getGcName"), "declares getGcName");
        check(ms.contains("toCompositeData"), "declares toCompositeData");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
