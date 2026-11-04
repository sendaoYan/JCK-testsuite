/*
 * @test
 * @summary javax.security.cert.X509Certificate conformance: abstract class hierarchy and key methods.
 * @run main X509CertificateTest
 */

public class X509CertificateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.security.cert.X509Certificate");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("javax.security.cert.Certificate"), "extends javax.security.cert.Certificate");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("checkValidity"), "declares checkValidity");
        check(ms.contains("getInstance"), "declares getInstance");
        check(ms.contains("getIssuerDN"), "declares getIssuerDN");
        check(ms.contains("getNotAfter"), "declares getNotAfter");
        check(ms.contains("getNotBefore"), "declares getNotBefore");
        check(ms.contains("getSerialNumber"), "declares getSerialNumber");
        check(ms.contains("getSigAlgName"), "declares getSigAlgName");
        check(ms.contains("getSigAlgOID"), "declares getSigAlgOID");
        check(ms.contains("getSigAlgParams"), "declares getSigAlgParams");
        check(ms.contains("getSubjectDN"), "declares getSubjectDN");
        check(ms.contains("getVersion"), "declares getVersion");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
