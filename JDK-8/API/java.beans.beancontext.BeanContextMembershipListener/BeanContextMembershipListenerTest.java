/*
 * @test
 * @summary java.beans.beancontext.BeanContextMembershipListener conformance: interface reflection.
 * @run main BeanContextMembershipListenerTest
 */

public class BeanContextMembershipListenerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextMembershipListener");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.util.EventListener").isAssignableFrom(c), "implements/extends java.util.EventListener");
        check(hasMethod(c, "childrenAdded"), "declares childrenAdded");
        check(hasMethod(c, "childrenRemoved"), "declares childrenRemoved");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
