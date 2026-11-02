/*
 * @test
 * @summary com.sun.security.cert.internal.x509.X509V1CertImpl conformance: hierarchy and declared members via reflection.
 * @run main X509V1CertImplTest
 */

public class X509V1CertImplTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.security.cert.internal.x509.X509V1CertImpl", false, null);
        check(c.getName().equals("com.sun.security.cert.internal.x509.X509V1CertImpl"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.security.cert.X509Certificate"), "superclass javax.security.cert.X509Certificate");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.io.Serializable"), "implements java.io.Serializable");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("checkValidity"), "declares checkValidity");
        check(ms.contains("getEncoded"), "declares getEncoded");
        check(ms.contains("getFactory"), "declares getFactory");
        check(ms.contains("getIssuerDN"), "declares getIssuerDN");
        check(ms.contains("getNotAfter"), "declares getNotAfter");
        check(ms.contains("getNotBefore"), "declares getNotBefore");
        check(ms.contains("getPublicKey"), "declares getPublicKey");
        check(ms.contains("getSerialNumber"), "declares getSerialNumber");
        check(ms.contains("getSigAlgName"), "declares getSigAlgName");
        check(ms.contains("getSigAlgOID"), "declares getSigAlgOID");
        check(ms.contains("getSigAlgParams"), "declares getSigAlgParams");
        check(ms.contains("getSubjectDN"), "declares getSubjectDN");
        check(ms.contains("getVersion"), "declares getVersion");
        check(ms.contains("getX509Certificate"), "declares getX509Certificate");
        check(ms.contains("readObject"), "declares readObject");
        check(ms.contains("toString"), "declares toString");
        check(ms.contains("verify"), "declares verify");
        check(ms.contains("writeObject"), "declares writeObject");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
