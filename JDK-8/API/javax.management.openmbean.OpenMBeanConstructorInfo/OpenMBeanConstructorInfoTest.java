/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanConstructorInfo conformance: interface reflection.
 * @run main OpenMBeanConstructorInfoTest
 */

public class OpenMBeanConstructorInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanConstructorInfo");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getDescription"), "declares getDescription");
        check(hasMethod(c, "getName"), "declares getName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
