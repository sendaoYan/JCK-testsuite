/*
 * @test
 * @summary javax.management.Descriptor conformance: interface reflection.
 * @run main DescriptorTest
 */

public class DescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.Descriptor");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(Class.forName("java.lang.Cloneable").isAssignableFrom(c), "implements/extends java.lang.Cloneable");
        check(hasMethod(c, "clone"), "declares clone");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getFieldNames"), "declares getFieldNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
