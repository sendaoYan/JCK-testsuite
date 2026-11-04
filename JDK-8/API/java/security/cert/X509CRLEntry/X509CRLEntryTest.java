/*
 * @test
 * @summary java.security.cert.X509CRLEntry conformance: abstract class hierarchy and key methods.
 * @run main X509CRLEntryTest
 */

public class X509CRLEntryTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.cert.X509CRLEntry");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.security.cert.X509Extension"), "implements java.security.cert.X509Extension");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("getCertificateIssuer"), "declares getCertificateIssuer");
        check(ms.contains("getEncoded"), "declares getEncoded");
        check(ms.contains("getRevocationDate"), "declares getRevocationDate");
        check(ms.contains("getRevocationReason"), "declares getRevocationReason");
        check(ms.contains("getSerialNumber"), "declares getSerialNumber");
        check(ms.contains("hasExtensions"), "declares hasExtensions");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("toString"), "declares toString");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
