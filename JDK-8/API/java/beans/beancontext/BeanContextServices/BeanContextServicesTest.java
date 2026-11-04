/*
 * @test
 * @summary java.beans.beancontext.BeanContextServices conformance: interface reflection.
 * @run main BeanContextServicesTest
 */

public class BeanContextServicesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServices");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.beans.beancontext.BeanContext").isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContext");
        check(Class.forName("java.beans.beancontext.BeanContextServicesListener").isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContextServicesListener");
        check(hasMethod(c, "addBeanContextServicesListener"), "declares addBeanContextServicesListener");
        check(hasMethod(c, "addService"), "declares addService");
        check(hasMethod(c, "getCurrentServiceClasses"), "declares getCurrentServiceClasses");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
