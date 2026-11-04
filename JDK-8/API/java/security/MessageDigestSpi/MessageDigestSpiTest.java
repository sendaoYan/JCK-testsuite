/*
 * @test
 * @summary java.security.MessageDigestSpi conformance: abstract class hierarchy and key methods.
 * @run main MessageDigestSpiTest
 */

public class MessageDigestSpiTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.MessageDigestSpi");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("clone"), "declares clone");
        check(ms.contains("engineDigest"), "declares engineDigest");
        check(ms.contains("engineGetDigestLength"), "declares engineGetDigestLength");
        check(ms.contains("engineReset"), "declares engineReset");
        check(ms.contains("engineUpdate"), "declares engineUpdate");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
