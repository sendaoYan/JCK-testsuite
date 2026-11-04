/*
 * @test
 * @summary javax.management.loading.ClassLoaderRepository conformance: interface reflection.
 * @run main ClassLoaderRepositoryTest
 */

public class ClassLoaderRepositoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.loading.ClassLoaderRepository");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "loadClass"), "declares loadClass");
        check(hasMethod(c, "loadClassBefore"), "declares loadClassBefore");
        check(hasMethod(c, "loadClassWithout"), "declares loadClassWithout");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
