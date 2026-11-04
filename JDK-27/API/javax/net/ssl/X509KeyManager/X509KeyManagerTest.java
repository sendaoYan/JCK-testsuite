/*
 * @test
 * @summary javax.net.ssl.X509KeyManager conformance: white-box structure + black-box behavior.
 * @run main X509KeyManagerTest
 */
import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.security.*;
import javax.net.ssl.*;
public class X509KeyManagerTest {
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
        Class<?> c = Class.forName("javax.net.ssl.X509KeyManager");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "interface");
        check(Arrays.asList(Class.forName("javax.net.ssl.X509KeyManager").getInterfaces()).contains(Class.forName("javax.net.ssl.KeyManager")), "extends javax.net.ssl.KeyManager");
        check(Arrays.stream(Class.forName("javax.net.ssl.X509KeyManager").getMethods()).anyMatch(m -> m.getName().equals("chooseServerAlias")), "method chooseServerAlias");
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        javax.net.ssl.KeyManagerFactory kmf = javax.net.ssl.KeyManagerFactory.getInstance(javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore(), PASS);
        javax.net.ssl.X509KeyManager xkm = (javax.net.ssl.X509KeyManager) kmf.getKeyManagers()[0];
        check(xkm instanceof javax.net.ssl.X509ExtendedKeyManager, "also X509ExtendedKeyManager");
        String alias = xkm.chooseServerAlias("RSA", null, null);
        check(alias != null, "server alias chosen");
        check(xkm.getCertificateChain(alias) != null && xkm.getCertificateChain(alias).length >= 1, "cert chain");
        check(xkm.getPrivateKey(alias) instanceof java.security.interfaces.RSAPrivateKey, "private key");
        check(xkm.getCertificateChain(alias)[0].getEncoded().length > 0, "cert encoded");
        String[] serverAliases = xkm.getServerAliases("RSA", null);
        check(serverAliases != null && serverAliases.length >= 1, "server aliases");
        check(xkm.chooseClientAlias(new String[]{"RSA"}, null, null) != null, "client alias chosen");
        check(xkm.getClientAliases("RSA", null) != null, "client aliases");
        check(xkm.chooseServerAlias("EC", null, null) == null, "no EC alias");
        check(xkm.getCertificateChain("nope") == null, "bogus alias chain null");
        check(xkm.getPrivateKey("nope") == null, "bogus alias key null");
    }
    // ---- Part 3: contract deepening (key/chain consistency + key-type matrix) ----
    static void testContract() throws Exception {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore(), PASS);
        X509KeyManager xkm = (X509KeyManager) kmf.getKeyManagers()[0];
        String alias = xkm.chooseServerAlias("RSA", null, null);
        check(alias != null, "RSA alias chosen");
        // the private key matches the certificate public key (RSA modulus equality)
        java.security.interfaces.RSAPrivateKey priv =
                (java.security.interfaces.RSAPrivateKey) xkm.getPrivateKey(alias);
        java.security.interfaces.RSAPublicKey pub = (java.security.interfaces.RSAPublicKey)
                xkm.getCertificateChain(alias)[0].getPublicKey();
        check(priv.getModulus().equals(pub.getModulus()), "private key modulus == certificate public key modulus");
        check(priv.getModulus().bitLength() == 2048, "2048-bit RSA key material");
        // issuer filter array is accepted (empty filter)
        check(xkm.chooseServerAlias("RSA", new javax.security.auth.x500.X500Principal[0], null) != null,
                "chooseServerAlias with empty issuer filter");
        // unsupported key types return null across the board
        check(xkm.chooseServerAlias("EC", null, null) == null, "chooseServerAlias EC -> null");
        check(xkm.getServerAliases("EC", null) == null, "getServerAliases EC -> null");
        check(xkm.chooseClientAlias(new String[]{"EC"}, null, null) == null,
                "chooseClientAlias EC -> null");
        check(xkm.getClientAliases("EC", null) == null, "getClientAliases EC -> null");
        // alias lookups are consistent for the same arguments
        check(java.util.Arrays.asList(xkm.getServerAliases("RSA", null)).contains(alias),
                "chooseServerAlias picks one of getServerAliases");
        check(xkm.getCertificateChain(alias).length == xkm.getCertificateChain(alias).length
                && xkm.getPrivateKey(alias) != null, "chain/key lookups repeatable");
        // interface method surface
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : X509KeyManager.class.getMethods()) names.add(m.getName());
        check(names.containsAll(java.util.Arrays.asList("chooseClientAlias", "getClientAliases",
                "chooseServerAlias", "getServerAliases", "getCertificateChain", "getPrivateKey")),
                "interface exposes the 6 selector methods");
    }
    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
