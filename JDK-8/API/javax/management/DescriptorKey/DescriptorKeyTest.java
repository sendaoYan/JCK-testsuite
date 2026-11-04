/*
 * @test
 * @summary javax.management.DescriptorKey conformance: annotation reflection.
 * @run main DescriptorKeyTest
 */

public class DescriptorKeyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.DescriptorKey");
        check(c.isAnnotation(), "annotation");
        check(c.isInterface(), "annotation is an interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.lang.annotation.Annotation").isAssignableFrom(c), "implements/extends java.lang.annotation.Annotation");
        check(hasMethod(c, "value"), "declares value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
