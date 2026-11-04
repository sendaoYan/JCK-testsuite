/*
 * @test
 * @summary javax.management.openmbean.OpenMBeanParameterInfo conformance: interface reflection.
 * @run main OpenMBeanParameterInfoTest
 */

public class OpenMBeanParameterInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.openmbean.OpenMBeanParameterInfo");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getDefaultValue"), "declares getDefaultValue");
        check(hasMethod(c, "getDescription"), "declares getDescription");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
