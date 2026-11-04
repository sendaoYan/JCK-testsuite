/*
 * @test
 * @summary java.security.KeyPairGenerator conformance: abstract class hierarchy and key methods.
 * @run main KeyPairGeneratorTest
 */

public class KeyPairGeneratorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.KeyPairGenerator");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("java.security.KeyPairGeneratorSpi"), "extends java.security.KeyPairGeneratorSpi");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("genKeyPair"), "declares genKeyPair");
        check(ms.contains("generateKeyPair"), "declares generateKeyPair");
        check(ms.contains("getAlgorithm"), "declares getAlgorithm");
        check(ms.contains("getInstance"), "declares getInstance");
        check(ms.contains("getProvider"), "declares getProvider");
        check(ms.contains("initialize"), "declares initialize");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
