/*
 * @test
 * @summary com.sun.jmx.mbeanserver.DynamicMBean2 conformance: hierarchy and declared members via reflection.
 * @run main DynamicMBean2Test
 */

public class DynamicMBean2Test {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.mbeanserver.DynamicMBean2", false, null);
        check(c.getName().equals("com.sun.jmx.mbeanserver.DynamicMBean2"), "binary name");
        check(c.isInterface() == true, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "is abstract");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("javax.management.DynamicMBean"), "implements javax.management.DynamicMBean");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getClassName"), "declares getClassName");
        check(ms.contains("getResource"), "declares getResource");
        check(ms.contains("preRegister2"), "declares preRegister2");
        check(ms.contains("registerFailed"), "declares registerFailed");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
