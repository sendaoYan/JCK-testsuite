/*
 * @test
 * @summary java.beans.beancontext.BeanContextServiceRevokedListener conformance: interface reflection.
 * @run main BeanContextServiceRevokedListenerTest
 */

public class BeanContextServiceRevokedListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextServiceRevokedListener");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.util.EventListener").isAssignableFrom(c), "implements/extends java.util.EventListener");
        check(hasMethod(c, "serviceRevoked"), "declares serviceRevoked");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
