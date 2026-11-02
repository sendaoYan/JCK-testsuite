/*
 * @test
 * @summary java.beans.beancontext.BeanContextServicesSupport conformance: concrete class reflection.
 * @run main BeanContextServicesSupportTest
 */

public class BeanContextServicesSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesSupport");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.beans.beancontext.BeanContextSupport".equals(c.getSuperclass().getName()), "superclass java.beans.beancontext.BeanContextSupport");
        check(Class.forName("java.beans.beancontext.BeanContextServices").isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContextServices");
        check(hasMethod(c, "addBeanContextServicesListener"), "declares addBeanContextServicesListener");
        check(hasMethod(c, "addService"), "declares addService");
        check(hasMethod(c, "getBeanContextServicesPeer"), "declares getBeanContextServicesPeer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
