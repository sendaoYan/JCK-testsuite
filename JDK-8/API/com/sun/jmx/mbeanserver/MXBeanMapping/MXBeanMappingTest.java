/*
 * @test
 * @summary com.sun.jmx.mbeanserver.MXBeanMapping conformance: hierarchy and declared members via reflection.
 * @run main MXBeanMappingTest
 */

public class MXBeanMappingTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.mbeanserver.MXBeanMapping", false, null);
        check(c.getName().equals("com.sun.jmx.mbeanserver.MXBeanMapping"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("checkReconstructible"), "declares checkReconstructible");
        check(ms.contains("fromOpenValue"), "declares fromOpenValue");
        check(ms.contains("getJavaType"), "declares getJavaType");
        check(ms.contains("getOpenClass"), "declares getOpenClass");
        check(ms.contains("getOpenType"), "declares getOpenType");
        check(ms.contains("makeOpenClass"), "declares makeOpenClass");
        check(ms.contains("toOpenValue"), "declares toOpenValue");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
