/*
 * @test
 * @summary java.beans.beancontext.BeanContextMembershipEvent conformance: concrete class reflection.
 * @run main BeanContextMembershipEventTest
 */

public class BeanContextMembershipEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextMembershipEvent");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.beans.beancontext.BeanContextEvent".equals(c.getSuperclass().getName()), "superclass java.beans.beancontext.BeanContextEvent");
        check(hasMethod(c, "contains"), "declares contains");
        check(hasMethod(c, "iterator"), "declares iterator");
        check(hasMethod(c, "size"), "declares size");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
