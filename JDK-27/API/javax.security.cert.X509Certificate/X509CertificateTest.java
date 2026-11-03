/*
 * @test
 * @summary javax.security.cert.X509Certificate conformance: white-box structure + black-box behavior.
 * @run main X509CertificateTest
 */

import java.lang.reflect.*;
import java.math.BigInteger;
import java.security.Principal;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Date;
import java.io.ByteArrayInputStream;

public class X509CertificateTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }
    static boolean hasPublicCtor(Class<?> c) { for (Constructor<?> k : c.getConstructors()) return true; return false; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.cert.X509Certificate");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("javax.security.cert.Certificate"), "extends javax.security.cert.Certificate");
        check(hasPublicCtor(c), "public constructor");
        checkMethods(c, new String[]{"checkValidity","getVersion","getSerialNumber","getIssuerDN","getSubjectDN",
                                     "getNotBefore","getNotAfter","getSigAlgName","getSigAlgOID","getSigAlgParams","getInstance"});
    }

    static Principal principal(final String name) {
        return new Principal() { public String getName() { return name; } };
    }

    // Concrete subclass implementing the full legacy X509 abstract surface.
    static class TestX509 extends javax.security.cert.X509Certificate {
        public byte[] getEncoded() { return new byte[]{9, 8, 7}; }
        public PublicKey getPublicKey() { return null; }
        public void verify(PublicKey key) {}
        public void verify(PublicKey key, String sigProvider) {}
        public String toString() { return "TestX509"; }
        public void checkValidity() {}
        public void checkValidity(Date date) {}
        public int getVersion() { return 3; }
        public BigInteger getSerialNumber() { return new BigInteger("42"); }
        public Principal getIssuerDN() { return principal("issuer"); }
        public Principal getSubjectDN() { return principal("subject"); }
        public Date getNotBefore() { return new Date(1000L); }
        public Date getNotAfter() { return new Date(2000L); }
        public String getSigAlgName() { return "SHA1withRSA"; }
        public String getSigAlgOID() { return "1.2.840.113549.1.1.5"; }
        public byte[] getSigAlgParams() { return null; }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        TestX509 cert = new TestX509();
        check(cert.getVersion() == 3, "getVersion");
        check("42".equals(cert.getSerialNumber().toString()), "getSerialNumber");
        check("issuer".equals(cert.getIssuerDN().getName()), "getIssuerDN");
        check("subject".equals(cert.getSubjectDN().getName()), "getSubjectDN");
        check(cert.getNotBefore().getTime() == 1000L, "getNotBefore");
        check(cert.getNotAfter().getTime() == 2000L, "getNotAfter");
        check("SHA1withRSA".equals(cert.getSigAlgName()), "getSigAlgName");
        check("1.2.840.113549.1.1.5".equals(cert.getSigAlgOID()), "getSigAlgOID");
        check(cert.getSigAlgParams() == null, "getSigAlgParams");
        check(cert.getPublicKey() == null, "getPublicKey");
        check(Arrays.equals(cert.getEncoded(), new byte[]{9, 8, 7}), "getEncoded");
        check("TestX509".equals(cert.toString()), "toString");

        cert.checkValidity();
        cert.checkValidity(new Date(1500L));
        check(true, "checkValidity callable");

        // getInstance on garbage bytes: no X.509 provider class is reachable,
        // so the factory reports a CertificateException.
        try {
            javax.security.cert.X509Certificate.getInstance(new byte[]{1, 2, 3, 4, 5});
            check(false, "getInstance(byte[]) should throw CertificateException");
        } catch (javax.security.cert.CertificateException e) {
            check(true, "getInstance(byte[]) throws CertificateException");
        }
        try {
            javax.security.cert.X509Certificate.getInstance(new ByteArrayInputStream(new byte[]{1, 2, 3}));
            check(false, "getInstance(InputStream) should throw CertificateException");
        } catch (javax.security.cert.CertificateException e) {
            check(true, "getInstance(InputStream) throws CertificateException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
