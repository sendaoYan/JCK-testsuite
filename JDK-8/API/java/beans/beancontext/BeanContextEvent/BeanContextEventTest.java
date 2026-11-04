/*
 * @test
 * @summary java.beans.beancontext.BeanContextEvent conformance: abstract class reflection.
 * @run main BeanContextEventTest
 */

public class BeanContextEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextEvent");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check("java.util.EventObject".equals(c.getSuperclass().getName()), "superclass java.util.EventObject");
        check(hasMethod(c, "getBeanContext"), "declares getBeanContext");
        check(hasMethod(c, "getPropagatedFrom"), "declares getPropagatedFrom");
        check(hasMethod(c, "isPropagated"), "declares isPropagated");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
