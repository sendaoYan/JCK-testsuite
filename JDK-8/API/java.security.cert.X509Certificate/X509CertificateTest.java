/*
 * @test
 * @summary java.security.cert.X509Certificate conformance: abstract class hierarchy and key methods.
 * @run main X509CertificateTest
 */

public class X509CertificateTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.cert.X509Certificate");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("java.security.cert.Certificate"), "extends java.security.cert.Certificate");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.security.cert.X509Extension"), "implements java.security.cert.X509Extension");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("checkValidity"), "declares checkValidity");
        check(ms.contains("getBasicConstraints"), "declares getBasicConstraints");
        check(ms.contains("getExtendedKeyUsage"), "declares getExtendedKeyUsage");
        check(ms.contains("getIssuerAlternativeNames"), "declares getIssuerAlternativeNames");
        check(ms.contains("getIssuerDN"), "declares getIssuerDN");
        check(ms.contains("getIssuerUniqueID"), "declares getIssuerUniqueID");
        check(ms.contains("getIssuerX500Principal"), "declares getIssuerX500Principal");
        check(ms.contains("getKeyUsage"), "declares getKeyUsage");
        check(ms.contains("getNotAfter"), "declares getNotAfter");
        check(ms.contains("getNotBefore"), "declares getNotBefore");
        check(ms.contains("getSerialNumber"), "declares getSerialNumber");
        check(ms.contains("getSigAlgName"), "declares getSigAlgName");
        check(ms.contains("getSigAlgOID"), "declares getSigAlgOID");
        check(ms.contains("getSigAlgParams"), "declares getSigAlgParams");
        check(ms.contains("getSignature"), "declares getSignature");
        check(ms.contains("getSubjectAlternativeNames"), "declares getSubjectAlternativeNames");
        check(ms.contains("getSubjectDN"), "declares getSubjectDN");
        check(ms.contains("getSubjectUniqueID"), "declares getSubjectUniqueID");
        check(ms.contains("getSubjectX500Principal"), "declares getSubjectX500Principal");
        check(ms.contains("getTBSCertificate"), "declares getTBSCertificate");
        check(ms.contains("getVersion"), "declares getVersion");
        check(ms.contains("verify"), "declares verify");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
