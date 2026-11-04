/*
 * @test
 * @summary javax.management.PersistentMBean conformance: interface reflection.
 * @run main PersistentMBeanTest
 */

public class PersistentMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.PersistentMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "load"), "declares load");
        check(hasMethod(c, "store"), "declares store");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
