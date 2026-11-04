/*
 * @test
 * @summary java.security.Certificate conformance: interface hierarchy and key methods.
 * @run main CertificateTest
 */

public class CertificateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.Certificate");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("decode"), "declares decode");
        check(ms.contains("encode"), "declares encode");
        check(ms.contains("getFormat"), "declares getFormat");
        check(ms.contains("getGuarantor"), "declares getGuarantor");
        check(ms.contains("getPrincipal"), "declares getPrincipal");
        check(ms.contains("getPublicKey"), "declares getPublicKey");
        check(ms.contains("toString"), "declares toString");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
