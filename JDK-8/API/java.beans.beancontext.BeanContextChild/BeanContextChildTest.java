/*
 * @test
 * @summary java.beans.beancontext.BeanContextChild conformance: interface reflection.
 * @run main BeanContextChildTest
 */

public class BeanContextChildTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.beans.beancontext.BeanContextChild");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "addPropertyChangeListener"), "declares addPropertyChangeListener");
        check(hasMethod(c, "addVetoableChangeListener"), "declares addVetoableChangeListener");
        check(hasMethod(c, "getBeanContext"), "declares getBeanContext");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
