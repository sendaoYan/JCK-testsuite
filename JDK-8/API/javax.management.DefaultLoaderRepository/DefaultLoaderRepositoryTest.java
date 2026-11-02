/*
 * @test
 * @summary javax.management.DefaultLoaderRepository conformance: concrete class reflection.
 * @run main DefaultLoaderRepositoryTest
 */

public class DefaultLoaderRepositoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.DefaultLoaderRepository");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(hasMethod(c, "loadClass"), "declares loadClass");
        check(hasMethod(c, "loadClassWithout"), "declares loadClassWithout");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
