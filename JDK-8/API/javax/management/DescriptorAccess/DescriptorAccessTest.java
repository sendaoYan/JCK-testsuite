/*
 * @test
 * @summary javax.management.DescriptorAccess conformance: interface reflection.
 * @run main DescriptorAccessTest
 */

public class DescriptorAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.DescriptorAccess");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.DescriptorRead").isAssignableFrom(c), "implements/extends javax.management.DescriptorRead");
        check(hasMethod(c, "setDescriptor"), "declares setDescriptor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
