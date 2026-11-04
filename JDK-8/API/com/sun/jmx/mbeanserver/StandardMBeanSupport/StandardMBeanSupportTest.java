/*
 * @test
 * @summary com.sun.jmx.mbeanserver.StandardMBeanSupport conformance: hierarchy and declared members via reflection.
 * @run main StandardMBeanSupportTest
 */

public class StandardMBeanSupportTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.mbeanserver.StandardMBeanSupport", false, null);
        check(c.getName().equals("com.sun.jmx.mbeanserver.StandardMBeanSupport"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.jmx.mbeanserver.MBeanSupport"), "superclass com.sun.jmx.mbeanserver.MBeanSupport");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getCookie"), "declares getCookie");
        check(ms.contains("getMBeanInfo"), "declares getMBeanInfo");
        check(ms.contains("getMBeanIntrospector"), "declares getMBeanIntrospector");
        check(ms.contains("register"), "declares register");
        check(ms.contains("unregister"), "declares unregister");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
