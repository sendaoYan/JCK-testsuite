/*
 * @test
 * @summary java.beans.beancontext.BeanContextServiceProvider conformance: interface reflection.
 * @run main BeanContextServiceProviderTest
 */

public class BeanContextServiceProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServiceProvider");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getCurrentServiceSelectors"), "declares getCurrentServiceSelectors");
        check(hasMethod(c, "getService"), "declares getService");
        check(hasMethod(c, "releaseService"), "declares releaseService");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
