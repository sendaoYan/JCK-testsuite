/*
 * @test
 * @summary java.rmi.server.RMIClassLoader conformance: loadClass and codebase helpers.
 * @run main RMIClassLoaderTest
 */

import java.rmi.server.RMIClassLoader;

public class RMIClassLoaderTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        check(RMIClassLoader.loadClass("java.lang.String") == String.class, "loadClass");
        check(RMIClassLoader.getClassAnnotation(String.class) == null, "bootstrap class annotation null");
        check(RMIClassLoader.getDefaultProviderInstance() != null, "default provider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
