/*
 * @test
 * @summary sun.security.provider.NativePRNG.Blocking conformance: type shape via reflection (headless-safe).
 * @run main BlockingTest
 */

import java.lang.reflect.Modifier;

public class BlockingTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.provider.NativePRNG$Blocking", false, BlockingTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        if (!System.getProperty("os.name", "").toLowerCase().contains("linux")) {
            System.out.println("PASS (" + passed + " assertions; SKIP build-specific internal structure)");
            return;
        }
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.SecureRandomSpi"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
