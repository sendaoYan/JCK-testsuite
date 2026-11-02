/*
 * @test
 * @summary java.beans.beancontext.BeanContextProxy conformance: interface reflection.
 * @run main BeanContextProxyTest
 */

public class BeanContextProxyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextProxy");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getBeanContextProxy"), "declares getBeanContextProxy");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
