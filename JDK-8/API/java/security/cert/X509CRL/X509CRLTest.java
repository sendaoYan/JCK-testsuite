/*
 * @test
 * @summary java.security.cert.X509CRL conformance: abstract class hierarchy and key methods.
 * @run main X509CRLTest
 */

public class X509CRLTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.cert.X509CRL");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == Class.forName("java.security.cert.CRL"), "extends java.security.cert.CRL");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.security.cert.X509Extension"), "implements java.security.cert.X509Extension");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("equals"), "declares equals");
        check(ms.contains("getEncoded"), "declares getEncoded");
        check(ms.contains("getIssuerDN"), "declares getIssuerDN");
        check(ms.contains("getIssuerX500Principal"), "declares getIssuerX500Principal");
        check(ms.contains("getNextUpdate"), "declares getNextUpdate");
        check(ms.contains("getRevokedCertificate"), "declares getRevokedCertificate");
        check(ms.contains("getSigAlgName"), "declares getSigAlgName");
        check(ms.contains("getSigAlgOID"), "declares getSigAlgOID");
        check(ms.contains("getSigAlgParams"), "declares getSigAlgParams");
        check(ms.contains("getSignature"), "declares getSignature");
        check(ms.contains("getTBSCertList"), "declares getTBSCertList");
        check(ms.contains("getThisUpdate"), "declares getThisUpdate");
        check(ms.contains("getVersion"), "declares getVersion");
        check(ms.contains("hashCode"), "declares hashCode");
        check(ms.contains("verify"), "declares verify");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
