/*
 * @test
 * @summary java.beans.beancontext.BeanContextServicesListener conformance: interface reflection.
 * @run main BeanContextServicesListenerTest
 */

public class BeanContextServicesListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServicesListener");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.beans.beancontext.BeanContextServiceRevokedListener").isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContextServiceRevokedListener");
        check(hasMethod(c, "serviceAvailable"), "declares serviceAvailable");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
