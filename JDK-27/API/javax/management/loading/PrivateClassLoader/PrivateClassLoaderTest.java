/*
 * @test
 * @summary javax.management.loading.PrivateClassLoader conformance: white-box structure + black-box behavior.
 * @run main PrivateClassLoaderTest
 */

import javax.management.loading.PrivateClassLoader;

public class PrivateClassLoaderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.loading.PrivateClassLoader");
        check(c.isInterface(), "is interface");
        check(c.getDeclaredMethods().length == 0, "marker interface has no methods");
        check(c.getDeclaredFields().length == 0, "marker interface has no fields");
    }

    // A ClassLoader implementing the marker interface satisfies both contracts.
    static class PrivateLoader extends ClassLoader implements PrivateClassLoader {
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ClassLoader loader = new PrivateLoader();
        check(loader instanceof ClassLoader, "instance of ClassLoader");
        check(loader instanceof PrivateClassLoader, "instance of PrivateClassLoader");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
