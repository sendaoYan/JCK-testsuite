/*
 * @test
 * @summary java.beans.BeanInfo conformance: interface reflection.
 * @run main BeanInfoTest
 */

public class BeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.BeanInfo");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getAdditionalBeanInfo"), "declares getAdditionalBeanInfo");
        check(hasMethod(c, "getBeanDescriptor"), "declares getBeanDescriptor");
        check(hasMethod(c, "getDefaultEventIndex"), "declares getDefaultEventIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
