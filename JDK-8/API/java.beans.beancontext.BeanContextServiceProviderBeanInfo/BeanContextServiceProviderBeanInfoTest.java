/*
 * @test
 * @summary java.beans.beancontext.BeanContextServiceProviderBeanInfo conformance: interface reflection.
 * @run main BeanContextServiceProviderBeanInfoTest
 */

public class BeanContextServiceProviderBeanInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServiceProviderBeanInfo");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.beans.BeanInfo").isAssignableFrom(c), "implements/extends java.beans.BeanInfo");
        check(hasMethod(c, "getServicesBeanInfo"), "declares getServicesBeanInfo");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
