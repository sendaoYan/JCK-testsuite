/*
 * @test
 * @summary javax.net.ssl.TrustManagerFactory conformance: white-box structure + black-box behavior.
 * @run main TrustManagerFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class TrustManagerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface Act { Object run() throws Exception; }
    static Object expect(Act a) { try { return a.run(); } catch (Throwable t) { return t.getClass().getSimpleName(); } }
    static final String B64 = "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";
    static final char[] PASS = "passphrase".toCharArray();
    static KeyStore keyStore() throws Exception {
        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(Base64.getDecoder().decode(B64)), PASS);
        return ks;
    }
    static SSLContext newContext() throws Exception {
        KeyStore ks = keyStore();
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(ks, PASS);
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        KeyStore ts = KeyStore.getInstance("PKCS12"); ts.load(null, null);
        ts.setCertificateEntry("server", ks.getCertificate("server"));
        tmf.init(ts);
        SSLContext ctx = SSLContext.getInstance("TLS");
        ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
        return ctx;
    }
    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.net.ssl.TrustManagerFactory");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface(), "not interface");
        check(Arrays.stream(Class.forName("javax.net.ssl.TrustManagerFactory").getMethods()).anyMatch(m -> m.getName().equals("getTrustManagers")), "method getTrustManagers");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        String alg = javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm();
        check(alg != null && !alg.isEmpty(), "default algorithm");
        javax.net.ssl.TrustManagerFactory tmf = javax.net.ssl.TrustManagerFactory.getInstance(alg);
        check(tmf.getAlgorithm().equals(alg), "algorithm roundtrip");
        check(tmf.getProvider() != null, "provider non-null");
        check(expect(() -> { javax.net.ssl.TrustManagerFactory.getInstance("BOGUS"); return "ok"; }).equals("NoSuchAlgorithmException"), "unknown algorithm");
        check(expect(() -> { tmf.getTrustManagers(); return "ok"; }).equals("IllegalStateException"), "getTrustManagers before init");
        javax.net.ssl.TrustManagerFactory tmf2 = javax.net.ssl.TrustManagerFactory.getInstance("PKIX");
        check(expect(() -> { tmf2.init((java.security.KeyStore) null); return "ok"; }).equals("ok"), "null keystore init ok");
        javax.net.ssl.TrustManager[] tms = tmf2.getTrustManagers();
        check(tms.length >= 1, "default trust managers non-empty");
        check(tms[0] instanceof javax.net.ssl.X509ExtendedTrustManager, "X509ExtendedTrustManager");
        java.security.KeyStore ts = java.security.KeyStore.getInstance("PKCS12");
        ts.load(null, null);
        ts.setCertificateEntry("server", keyStore().getCertificate("server"));
        javax.net.ssl.TrustManagerFactory tmf3 = javax.net.ssl.TrustManagerFactory.getInstance("PKIX");
        tmf3.init(ts);
        check(tmf3.getTrustManagers().length >= 1, "custom trust managers");
    }
    // ---- Part 3: contract deepening (algorithm matrix + parameter objects) ----
    static void testContract() throws Exception {
        check("PKIX".equals(TrustManagerFactory.getDefaultAlgorithm()),
                "default algorithm is PKIX");
        // for trust managers both SunX509 and X509 resolve (unlike key managers)
        for (String alg : new String[]{"SunX509", "X509", "PKIX"}) {
            TrustManagerFactory t = TrustManagerFactory.getInstance(alg);
            check(t.getAlgorithm().equals(alg), "getInstance(" + alg + ") echoes the algorithm");
        }
        check(expect(() -> { TrustManagerFactory.getInstance(null); return "ok"; })
                .equals("NullPointerException"), "getInstance(null) -> NullPointerException");
        TrustManagerFactory base = TrustManagerFactory.getInstance("PKIX");
        TrustManagerFactory byName = TrustManagerFactory.getInstance("PKIX", base.getProvider().getName());
        check(byName.getProvider().equals(base.getProvider()), "provider-name overload resolves the same provider");
        TrustManagerFactory byObj = TrustManagerFactory.getInstance("PKIX", base.getProvider());
        check(byObj.getProvider().equals(base.getProvider()), "provider-object overload keeps the provider");
        check(expect(() -> { TrustManagerFactory.getInstance("PKIX", "NoSuchProviderXYZ"); return "ok"; })
                .equals("NoSuchProviderException"), "unknown provider name -> NoSuchProviderException");
        // PKIX accepts CertPathTrustManagerParameters
        java.security.cert.X509Certificate cert =
                (java.security.cert.X509Certificate) keyStore().getCertificate("server");
        java.security.cert.PKIXBuilderParameters pb = new java.security.cert.PKIXBuilderParameters(
                java.util.Collections.singleton(new java.security.cert.TrustAnchor(cert, null)),
                new java.security.cert.X509CertSelector());
        TrustManagerFactory cpTmf = TrustManagerFactory.getInstance("PKIX");
        cpTmf.init(new CertPathTrustManagerParameters(pb));
        check(cpTmf.getTrustManagers().length >= 1, "PKIX accepts CertPathTrustManagerParameters");
        // a generic ManagerFactoryParameters implementation is rejected
        TrustManagerFactory gen = TrustManagerFactory.getInstance("PKIX");
        class MFP implements ManagerFactoryParameters {}
        check(expect(() -> { gen.init(new MFP()); return "ok"; })
                .equals("InvalidAlgorithmParameterException"),
                "generic ManagerFactoryParameters -> InvalidAlgorithmParameterException");
        // each get call returns a fresh array wrapping the same manager
        TrustManagerFactory t = TrustManagerFactory.getInstance("PKIX");
        t.init((java.security.KeyStore) null);
        TrustManager[] m1 = t.getTrustManagers(), m2 = t.getTrustManagers();
        check(m1 != m2 && m1[0] == m2[0], "getTrustManagers: fresh array, same manager instance");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
