/*
 * @test
 * @summary javax.management.ValueExp conformance: interface reflection.
 * @run main ValueExpTest
 */

public class ValueExpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.ValueExp");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(hasMethod(c, "apply"), "declares apply");
        check(hasMethod(c, "setMBeanServer"), "declares setMBeanServer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
