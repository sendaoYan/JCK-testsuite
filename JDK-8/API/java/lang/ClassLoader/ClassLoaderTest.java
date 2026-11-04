/*
 * @test
 * @summary java.lang.ClassLoader API conformance: getSystemClassLoader, getSystemResource,
 *          loadClass of a bootstrap class, and non-null loader of the test class.
 * @run main ClassLoaderTest
 */

public class ClassLoaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testClassLoader() {
        ClassLoader system = ClassLoader.getSystemClassLoader();
        check(system != null, "getSystemClassLoader non-null");
        check(ClassLoader.getSystemResource("java/lang/String.class") != null,
              "getSystemResource");
        try {
            check(system.loadClass("java.lang.String") == String.class, "loadClass");
        } catch (ClassNotFoundException e) {
            throw new AssertionError(e);
        }
        check(ClassLoaderTest.class.getClassLoader() != null, "test class has non-null loader");
    }

    public static void main(String[] args) {
        testClassLoader();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
