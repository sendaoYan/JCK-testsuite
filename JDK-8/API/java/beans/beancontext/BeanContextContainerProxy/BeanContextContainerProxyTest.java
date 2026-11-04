/*
 * @test
 * @summary java.beans.beancontext.BeanContextContainerProxy conformance: interface reflection.
 * @run main BeanContextContainerProxyTest
 */

public class BeanContextContainerProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextContainerProxy");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getContainer"), "declares getContainer");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
