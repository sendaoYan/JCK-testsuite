/*
 * @test
 * @summary java.security.MessageDigest conformance: abstract class hierarchy and key methods.
 * @run main MessageDigestTest
 */

public class MessageDigestTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.MessageDigest");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("java.security.MessageDigestSpi"), "extends java.security.MessageDigestSpi");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("clone"), "declares clone");
        check(ms.contains("digest"), "declares digest");
        check(ms.contains("getAlgorithm"), "declares getAlgorithm");
        check(ms.contains("getDigestLength"), "declares getDigestLength");
        check(ms.contains("getInstance"), "declares getInstance");
        check(ms.contains("getProvider"), "declares getProvider");
        check(ms.contains("isEqual"), "declares isEqual");
        check(ms.contains("reset"), "declares reset");
        check(ms.contains("toString"), "declares toString");
        check(ms.contains("update"), "declares update");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
