/*
 * @test
 * @summary javax.security.auth.x500.X500PrivateCredential conformance: white-box structure + black-box behavior.
 * @run main X500PrivateCredentialTest
 */

import java.lang.reflect.Modifier;
import java.math.BigInteger;
import java.security.Principal;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Date;
import javax.security.auth.x500.X500PrivateCredential;

public class X500PrivateCredentialTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.security.auth.x500.X500PrivateCredential");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(!c.isInterface(), "not interface");
        check(hasInterface(c, "javax.security.auth.Destroyable"), "implements Destroyable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        X509Certificate cert = new FakeCert();
        PrivateKey key = new FakeKey();

        // null argument rejection
        checkIllegalArgument(null, key, "null certificate");
        checkIllegalArgument(cert, null, "null private key");
        checkIllegalArgument(cert, key, null, "null alias");

        // two-arg constructor: alias is null
        X500PrivateCredential c2 = new X500PrivateCredential(cert, key);
        check(c2.getCertificate() == cert, "getCertificate returns cert");
        check(c2.getPrivateKey() == key, "getPrivateKey returns key");
        check(c2.getAlias() == null, "two-arg getAlias null");
        check(!c2.isDestroyed(), "initially not destroyed");

        // three-arg constructor: alias retained
        X500PrivateCredential c3 = new X500PrivateCredential(cert, key, "myAlias");
        check(c3.getCertificate() == cert, "three-arg getCertificate returns cert");
        check(c3.getPrivateKey() == key, "three-arg getPrivateKey returns key");
        check("myAlias".equals(c3.getAlias()), "three-arg getAlias");
        check(!c3.isDestroyed(), "three-arg initially not destroyed");

        // destroy clears all references
        c3.destroy();
        check(c3.isDestroyed(), "destroyed after destroy");
        check(c3.getCertificate() == null, "getCertificate null after destroy");
        check(c3.getPrivateKey() == null, "getPrivateKey null after destroy");
        check(c3.getAlias() == null, "getAlias null after destroy");

        // destroy is idempotent
        c3.destroy();
        check(c3.isDestroyed(), "destroy idempotent");
    }

    static void checkIllegalArgument(X509Certificate cert, PrivateKey key, String label) {
        boolean iae = false;
        try { new X500PrivateCredential(cert, key); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, label + " throws IllegalArgumentException");
    }

    static void checkIllegalArgument(X509Certificate cert, PrivateKey key, String alias, String label) {
        boolean iae = false;
        try { new X500PrivateCredential(cert, key, alias); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, label + " throws IllegalArgumentException");
    }

    // Minimal, deterministic stand-in for a certificate: X500PrivateCredential
    // only stores and returns it, so only the abstract methods need bodies.
    static class FakeCert extends X509Certificate {
        public void checkValidity() { }
        public void checkValidity(Date date) { }
        public int getVersion() { return 3; }
        public BigInteger getSerialNumber() { return BigInteger.ONE; }
        public Principal getIssuerDN() { return new javax.security.auth.x500.X500Principal("CN=Issuer"); }
        public Principal getSubjectDN() { return new javax.security.auth.x500.X500Principal("CN=Subject"); }
        public Date getNotBefore() { return new Date(0L); }
        public Date getNotAfter() { return new Date(0L); }
        public byte[] getTBSCertificate() { return new byte[0]; }
        public byte[] getSignature() { return new byte[0]; }
        public String getSigAlgName() { return "FAKE"; }
        public String getSigAlgOID() { return "1.2.3"; }
        public byte[] getSigAlgParams() { return new byte[0]; }
        public boolean[] getIssuerUniqueID() { return null; }
        public boolean[] getSubjectUniqueID() { return null; }
        public boolean[] getKeyUsage() { return null; }
        public int getBasicConstraints() { return -1; }
        public boolean hasUnsupportedCriticalExtension() { return false; }
        public java.util.Set<String> getCriticalExtensionOIDs() { return null; }
        public java.util.Set<String> getNonCriticalExtensionOIDs() { return null; }
        public byte[] getExtensionValue(String oid) { return null; }
        public byte[] getEncoded() { return new byte[0]; }
        public void verify(PublicKey key) throws CertificateException { throw new CertificateException("fake"); }
        public void verify(PublicKey key, String sigProvider) throws CertificateException { throw new CertificateException("fake"); }
        public String toString() { return "FakeCert"; }
        public PublicKey getPublicKey() { return null; }
    }

    static class FakeKey implements PrivateKey {
        public String getAlgorithm() { return "FAKE"; }
        public String getFormat() { return "FAKE"; }
        public byte[] getEncoded() { return new byte[] { 1, 2, 3 }; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
