/*
 * @test
 * @summary java.security.Signature conformance: abstract class hierarchy and key methods.
 * @run main SignatureTest
 */

public class SignatureTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.Signature");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("java.security.SignatureSpi"), "extends java.security.SignatureSpi");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("clone"), "declares clone");
        check(ms.contains("getAlgorithm"), "declares getAlgorithm");
        check(ms.contains("getInstance"), "declares getInstance");
        check(ms.contains("getParameter"), "declares getParameter");
        check(ms.contains("getParameters"), "declares getParameters");
        check(ms.contains("getProvider"), "declares getProvider");
        check(ms.contains("initSign"), "declares initSign");
        check(ms.contains("initVerify"), "declares initVerify");
        check(ms.contains("setParameter"), "declares setParameter");
        check(ms.contains("sign"), "declares sign");
        check(ms.contains("toString"), "declares toString");
        check(ms.contains("update"), "declares update");
        check(ms.contains("verify"), "declares verify");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
