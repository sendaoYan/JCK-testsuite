/*
 * @test
 * @summary javax.management.DynamicMBean conformance: interface reflection.
 * @run main DynamicMBeanTest
 */

public class DynamicMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.DynamicMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getAttribute"), "declares getAttribute");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
        check(hasMethod(c, "getMBeanInfo"), "declares getMBeanInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
