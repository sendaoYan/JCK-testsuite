/*
 * @test
 * @summary JVMS 5.1/5.3/5.5 (Java SE 8): Class.forName loads a class, creates a
 *          single Class object per loader, and triggers initialization (the static
 *          initializer runs exactly once) on the first active use.
 * @run main ClassLoadingTest
 */

public class ClassLoadingTest {
    static class Loaded {
        static int initCount = 0;
        static { initCount++; }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("ClassLoadingTest$Loaded");
        if (c == null) throw new AssertionError("class not loaded");
        if (Loaded.initCount != 1) throw new AssertionError("initialization once: " + Loaded.initCount);

        // A second load returns the same Class object and does not reinitialize.
        Class<?> c2 = Class.forName("ClassLoadingTest$Loaded");
        if (c2 != c) throw new AssertionError("same Class object per loader");
        if (Loaded.initCount != 1) throw new AssertionError("no re-initialization");

        ClassLoader cl = ClassLoadingTest.class.getClassLoader();
        if (cl == null) throw new AssertionError("application class loader present");

        System.out.println("PASS");
    }
}
