/*
 * @test
 * @summary jdk.security.jarsigner.JarSigner.Builder conformance: jarsigner builder methods and defaults.
 * @run main BuilderTest
 */

import jdk.security.jarsigner.*;
import java.security.*;
import java.lang.reflect.*;

public class BuilderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] ns) { for (String n : ns) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] ns) { for (String n : ns) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasPublicCtor(Class<?> c) { for (Constructor<?> k : c.getConstructors()) return true; return false; }
    static void checkField(Class<?> c, String n) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) { check(true, "field " + n); return; } check(false, "field " + n); }

    static Object[] selfSigned() throws Exception {
        java.io.File ks = java.io.File.createTempFile("ksbdeepen", ".p12");
        ks.delete();
        ks.deleteOnExit();
        ProcessBuilder pb = new ProcessBuilder(System.getProperty("java.home") + "/bin/keytool",
                "-genkeypair", "-alias", "t", "-dname", "CN=UnitTest", "-storetype", "PKCS12",
                "-storepass", "changeit", "-keystore", ks.getAbsolutePath(),
                "-keyalg", "RSA", "-keysize", "2048", "-validity", "1");
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        proc.getInputStream().readAllBytes();
        check(proc.waitFor() == 0, "keytool exit 0");
        java.security.KeyStore store = java.security.KeyStore.getInstance(ks, "changeit".toCharArray());
        java.security.PrivateKey pk = (java.security.PrivateKey) store.getKey("t", "changeit".toCharArray());
        java.security.cert.Certificate[] chain = store.getCertificateChain("t");
        java.security.cert.CertPath cp = java.security.cert.CertificateFactory.getInstance("X.509")
                .generateCertPath(java.util.Arrays.asList(chain));
        return new Object[] {pk, cp};
    }

    static void testDeepen() throws Exception {
        Object[] mat = selfSigned();
        PrivateKey pk = (PrivateKey) mat[0];
        java.security.cert.CertPath certPath = (java.security.cert.CertPath) mat[1];
        JarSigner.Builder b = new JarSigner.Builder(pk, certPath);
        check(b.digestAlgorithm("SHA-256") instanceof JarSigner.Builder, "digestAlgorithm chains");
        check(b.signatureAlgorithm("SHA256withRSA") instanceof JarSigner.Builder, "signatureAlgorithm chains");
        check(b.signerName("n") instanceof JarSigner.Builder, "signerName chains");
        check(b.tsa(java.net.URI.create("https://example.invalid")) instanceof JarSigner.Builder, "tsa chains");
        JarSigner s1 = b.build();
        check(s1 != b.build(), "build returns fresh instances");
        check(s1.getTsa().toString().equals("https://example.invalid"), "configured tsa echoed");
        check(s1.getSignerName().equals("N"), "signer name echoed");
        boolean iae = false;
        try { new JarSigner.Builder(pk, certPath).signerName("bad name!"); }
        catch (IllegalArgumentException e) { iae = e.getMessage().equals("Name too long"); }
        check(iae, "signerName space IAE exact message");
        iae = false;
        try { new JarSigner.Builder(pk, certPath).signerName(""); } catch (IllegalArgumentException e) { iae = true; }
        check(iae, "empty signerName IAE");
        boolean nsae = false;
        try { new JarSigner.Builder(pk, certPath).digestAlgorithm("NOTADIGEST"); }
        catch (java.security.NoSuchAlgorithmException e) { nsae = e.getMessage().equals("NOTADIGEST MessageDigest not available"); }
        check(nsae, "bad digest NSEA exact message");
        nsae = false;
        try { new JarSigner.Builder(pk, certPath).signatureAlgorithm("NOTASIG"); }
        catch (java.security.NoSuchAlgorithmException e) { nsae = e.getMessage().equals("NOTASIG Signature not available"); }
        check(nsae, "bad sig NSEA exact message");
        iae = false;
        try { new JarSigner.Builder(pk, certPath).signatureAlgorithm("SHA256withECDSA"); }
        catch (IllegalArgumentException e) { iae = e.getMessage().equals("key algorithm not compatible with signature algorithm"); }
        check(iae, "key/sig mismatch IAE exact message");
        boolean uoe = false;
        try { new JarSigner.Builder(pk, certPath).setProperty("bogus", "v"); }
        catch (UnsupportedOperationException e) { uoe = e.getMessage().equals("Unsupported key bogus"); }
        check(uoe, "setProperty bogus UOE exact message");
        boolean npe = false;
        try { new JarSigner.Builder(pk, certPath).tsa(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "tsa null NPE");
        boolean npe2 = false;
        try { new JarSigner.Builder(null, certPath); } catch (NullPointerException e) { npe2 = true; }
        check(npe2, "null private key NPE");
        boolean npe3 = false;
        try { new JarSigner.Builder(pk, null); } catch (NullPointerException e) { npe3 = true; }
        check(npe3, "null certpath NPE");
        check(new JarSigner.Builder(pk, certPath).setProperty("internalsf", "true").build().getProperty("internalsf").equals("true"),
                "setProperty internalsf round-trip");
        check(new JarSigner.Builder(pk, certPath).setProperty("sectionsonly", "true").build().getProperty("sectionsonly").equals("true"),
                "setProperty sectionsonly round-trip");
        check(new JarSigner.Builder(pk, certPath).build().getSignerName().equals("SIGNER"),
                "default signer name upper-cases common name");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.security.jarsigner.JarSigner$Builder");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        checkMethods(c, new String[]{"digestAlgorithm", "signatureAlgorithm", "tsa", "signerName", "eventHandler", "setProperty", "build", "getDefaultDigestAlgorithm", "getDefaultSignatureAlgorithm"});
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC");
        KeyPair kp = kpg.generateKeyPair();
        check(JarSigner.Builder.getDefaultDigestAlgorithm() != null, "default digest algorithm non-null");
        check(JarSigner.Builder.getDefaultSignatureAlgorithm(kp.getPrivate()) != null, "default signature algorithm non-null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
