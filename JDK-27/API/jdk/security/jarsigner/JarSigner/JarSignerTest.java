/*
 * @test
 * @summary jdk.security.jarsigner.JarSigner conformance: white-box structure + black-box behavior.
 * @run main JarSignerTest
 */

import jdk.security.jarsigner.*;
import java.security.*;
import java.lang.reflect.*;
import java.util.*;
public class JarSignerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.security.jarsigner.JarSigner");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getConstructors().length == 0, "no public constructor");
        check(hasMethod(c, "sign"), "sign");
        check(hasMethod(c, "getDigestAlgorithm"), "getDigestAlgorithm");
        check(hasMethod(c, "getSignatureAlgorithm"), "getSignatureAlgorithm");
        check(hasMethod(c, "getTsa"), "getTsa");
        check(hasMethod(c, "getSignerName"), "getSignerName");
        check(hasMethod(c, "getProperty"), "getProperty");

    }

    static void testBlackBox() throws Exception {
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
        kpg.initialize(2048);
        PrivateKey pk = kpg.generateKeyPair().getPrivate();
        check(JarSigner.Builder.getDefaultDigestAlgorithm() != null, "default digest algorithm");
        check(JarSigner.Builder.getDefaultSignatureAlgorithm(pk) != null, "default signature algorithm");

    }


    static Object[] selfSigned() throws Exception {
        java.io.File ks = java.io.File.createTempFile("ksdeepen", ".p12");
        ks.delete();
        ks.deleteOnExit();
        ProcessBuilder pb = new ProcessBuilder(System.getProperty("java.home") + "/bin/keytool",
                "-genkeypair", "-alias", "t", "-dname", "CN=UnitTest", "-storetype", "PKCS12",
                "-storepass", "changeit", "-keystore", ks.getAbsolutePath(),
                "-keyalg", "RSA", "-keysize", "2048", "-validity", "1");
        pb.redirectErrorStream(true);
        Process proc = pb.start();
        proc.getInputStream().readAllBytes();
        check(proc.waitFor() == 0, "keytool genkeypair exit 0");
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
        check(certPath.getType().equals("X.509") && certPath.getCertificates().size() == 1, "self-signed certpath");
        check(JarSigner.Builder.getDefaultDigestAlgorithm().equals("SHA-384"), "default digest SHA-384");
        check(JarSigner.Builder.getDefaultSignatureAlgorithm(pk).equals("SHA384withRSA"), "default RSA sig");
        check(JarSigner.Builder.getDefaultSignatureAlgorithm(
                KeyPairGenerator.getInstance("EC").generateKeyPair().getPrivate()).equals("SHA384withECDSA"), "default EC sig");
        check(JarSigner.Builder.getDefaultSignatureAlgorithm(
                KeyPairGenerator.getInstance("DSA").generateKeyPair().getPrivate()).equals("SHA256withDSA"), "default DSA sig");
        java.util.List<String> events = new ArrayList<>();
        JarSigner signer = new JarSigner.Builder(pk, certPath)
                .digestAlgorithm("SHA-256")
                .signatureAlgorithm("SHA256withRSA")
                .signerName("unittest")
                .eventHandler((action, filename) -> events.add(action + ":" + filename))
                .build();
        check(signer.getDigestAlgorithm().equals("SHA-256"), "configured digest echoed");
        check(signer.getSignatureAlgorithm().equals("SHA256withRSA"), "configured sig echoed");
        check(signer.getTsa() == null, "no tsa by default");
        check(signer.getSignerName().equals("UNITTEST"), "signer name upper-cased");
        check(signer.getProperty("internalsf").equals("false"), "internalsf default false");
        check(signer.getProperty("sectionsonly").equals("false"), "sectionsonly default false");
        check(signer.getProperty("tsadigestalg").equals("SHA-384"), "tsadigestalg default");
        check(signer.getProperty("tsapolicyid") == null, "tsapolicyid null default");
        boolean uoe = false;
        try { signer.getProperty("bogus"); } catch (UnsupportedOperationException e) {
            uoe = e.getMessage().equals("Unsupported key bogus");
        }
        check(uoe, "getProperty bogus UOE exact message");
        boolean npe = false;
        try { signer.getProperty(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "getProperty null NPE");
        // real sign loop
        java.io.File jar = java.io.File.createTempFile("unsigneddeepen", ".jar");
        jar.deleteOnExit();
        try (java.util.jar.JarOutputStream jos = new java.util.jar.JarOutputStream(new java.io.FileOutputStream(jar))) {
            jos.putNextEntry(new java.util.jar.JarEntry("com/foo.txt"));
            jos.write("hello jar".getBytes("UTF-8"));
            jos.closeEntry();
        }
        java.io.File signed = java.io.File.createTempFile("signeddeepen", ".jar");
        signed.deleteOnExit();
        java.io.OutputStream os = new java.io.FileOutputStream(signed);
        try (java.util.zip.ZipFile zf = new java.util.zip.ZipFile(jar)) { signer.sign(zf, os); }
        os.close();
        check(signed.length() > jar.length(), "signed jar larger than unsigned");
        java.util.Collections.sort(events);
        check(events.size() == 4, "four signing events");
        check(events.get(0).equals("adding:META-INF/MANIFEST.MF"), "first event manifest");
        try (java.util.jar.JarFile jf = new java.util.jar.JarFile(signed)) {
            check(jf.getEntry("META-INF/MANIFEST.MF") != null, "manifest present");
            check(jf.getEntry("META-INF/UNITTEST.SF") != null, "SF named by signer");
            check(jf.getEntry("META-INF/UNITTEST.RSA") != null, "RSA block present");
            check(new String(jf.getInputStream(new java.util.jar.JarEntry("com/foo.txt")).readAllBytes(), "UTF-8").equals("hello jar"),
                    "payload byte-identical");
        }
        try (java.util.jar.JarFile jf = new java.util.jar.JarFile(signed, true)) {
            java.util.jar.JarEntry e = jf.getJarEntry("com/foo.txt");
            jf.getInputStream(e).readAllBytes();
            check(e.getCodeSigners() != null && e.getCodeSigners().length == 1, "JarFile verifies one code signer");
        }
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
