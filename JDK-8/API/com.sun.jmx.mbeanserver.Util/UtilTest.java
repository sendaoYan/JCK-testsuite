/*
 * @test
 * @summary com.sun.jmx.mbeanserver.Util conformance: hierarchy and declared members via reflection.
 * @run main UtilTest
 */

public class UtilTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.mbeanserver.Util", false, null);
        check(c.getName().equals("com.sun.jmx.mbeanserver.Util"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("cast"), "declares cast");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("newIdentityHashMap"), "declares newIdentityHashMap");
        check(ms.contains("newInsertionOrderMap"), "declares newInsertionOrderMap");
        check(ms.contains("newList"), "declares newList");
        check(ms.contains("newMap"), "declares newMap");
        check(ms.contains("newObjectName"), "declares newObjectName");
        check(ms.contains("newSet"), "declares newSet");
        check(ms.contains("newSortedMap"), "declares newSortedMap");
        check(ms.contains("newSynchronizedIdentityHashMap"), "declares newSynchronizedIdentityHashMap");
        check(ms.contains("newSynchronizedMap"), "declares newSynchronizedMap");
        check(ms.contains("wildmatch"), "declares wildmatch");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
