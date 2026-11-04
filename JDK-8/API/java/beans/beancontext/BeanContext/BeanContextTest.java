/*
 * @test
 * @summary java.beans.beancontext.BeanContext conformance: interface reflection.
 * @run main BeanContextTest
 */

public class BeanContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContext");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.beans.beancontext.BeanContextChild").isAssignableFrom(c), "implements/extends java.beans.beancontext.BeanContextChild");
        check(Class.forName("java.util.Collection").isAssignableFrom(c), "implements/extends java.util.Collection");
        check(Class.forName("java.beans.DesignMode").isAssignableFrom(c), "implements/extends java.beans.DesignMode");
        check(Class.forName("java.beans.Visibility").isAssignableFrom(c), "implements/extends java.beans.Visibility");
        check(hasMethod(c, "addBeanContextMembershipListener"), "declares addBeanContextMembershipListener");
        check(hasMethod(c, "getResource"), "declares getResource");
        check(hasMethod(c, "getResourceAsStream"), "declares getResourceAsStream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
