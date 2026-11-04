/*
 * @test
 * @summary java.security.SecureClassLoader conformance: subclassable, extends ClassLoader.
 * @run main SecureClassLoaderTest
 */

public class SecureClassLoaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.SecureClassLoader scl = new java.security.SecureClassLoader() {};
        check(scl instanceof java.lang.ClassLoader, "extends ClassLoader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
