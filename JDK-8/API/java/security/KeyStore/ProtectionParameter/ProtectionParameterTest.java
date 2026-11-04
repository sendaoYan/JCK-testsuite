/*
 * @test
 * @summary java.security.KeyStore.ProtectionParameter conformance: marker
 *          interface structure, anonymous implementations, and the two JDK
 *          implementations (PasswordProtection / CallbackHandlerProtection)
 *          flowing through the supertype.
 * @run main ProtectionParameterTest
 */

public class ProtectionParameterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.KeyStore$ProtectionParameter");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended) ----
    static void testContractDeep() throws Exception {
        Class<?> c = Class.forName("java.security.KeyStore$ProtectionParameter");

        // marker interface: no methods of its own beyond Object
        java.lang.reflect.Method[] own = c.getMethods();
        int ownCount = 0;
        for (java.lang.reflect.Method m : own) {
            if (m.getDeclaringClass() == c) ownCount++;
        }
        check(ownCount == 0, "marker interface declares no methods");
        check(c.getDeclaredMethods().length == 0, "no declared methods");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public interface");

        // anonymous implementations
        java.security.KeyStore.ProtectionParameter anon =
                new java.security.KeyStore.ProtectionParameter() { };
        check(anon != null, "anonymous implementation instantiable");
        check(c.isInstance(anon), "anonymous instanceof ProtectionParameter");
        java.security.KeyStore.ProtectionParameter anon2 =
                new java.security.KeyStore.ProtectionParameter() { };
        check(!anon.equals(anon2), "anonymous instances use identity equality");
        check(!anon.equals(null), "anonymous marker not equal to null");

        // both JDK implementations flow through the supertype
        java.security.KeyStore.ProtectionParameter pw =
                new java.security.KeyStore.PasswordProtection("x".toCharArray());
        java.security.KeyStore.ProtectionParameter ch = new java.security.KeyStore.CallbackHandlerProtection(
                callbacks -> { });
        check(c.isInstance(pw), "PasswordProtection implements ProtectionParameter");
        check(c.isInstance(ch), "CallbackHandlerProtection implements ProtectionParameter");

        // array covariance through the marker type
        java.security.KeyStore.ProtectionParameter[] params = {pw, ch, anon};
        check(params.length == 3, "parameter array covariance");
        check(c.isInstance(params[0]) && c.isInstance(params[1]) && c.isInstance(params[2]),
                "all array members are ProtectionParameters");

        // accepted as Builder protection input
        java.security.KeyStore ks = java.security.KeyStore.getInstance("JKS");
        ks.load(null, null);
        java.security.KeyStore.Builder b = java.security.KeyStore.Builder.newInstance(ks, pw);
        check(b.getKeyStore() == ks, "marker value accepted by Builder");
        check(b.getProtectionParameter("a") == pw, "builder returns the same parameter");

        // null is a documented value for setEntry of trusted-cert entries only;
        // for private keys the engine rejects null (KeyStoreException)
        boolean rejected = false;
        try {
            ks.setEntry("p", new java.security.KeyStore.PrivateKeyEntry(
                    new FauxPrivateKey(), new java.security.cert.Certificate[]{new FauxCert()}),
                    null);
        } catch (java.security.KeyStoreException kse) {
            rejected = true;
        }
        check(rejected, "null ProtectionParameter rejected for private key setEntry");

        // PasswordProtection's Destroyable lifecycle is reachable through the marker type
        java.security.KeyStore.PasswordProtection destroyable =
                new java.security.KeyStore.PasswordProtection("z".toCharArray());
        check(c.isInstance(destroyable), "destroyable instance is marker-typed");
        destroyable.destroy();
        check(destroyable.isDestroyed(), "destroy via marker-typed reference");

        // DomainLoadStoreParameter's per-keystore map is typed by the marker
        java.util.Map<String, java.security.KeyStore.ProtectionParameter> dmap =
                new java.util.LinkedHashMap<String, java.security.KeyStore.ProtectionParameter>();
        dmap.put("keystore", pw);
        java.security.DomainLoadStoreParameter d = new java.security.DomainLoadStoreParameter(
                java.net.URI.create("file:/x.cfg"), dmap);
        check(d.getProtectionParams().get("keystore") == pw,
                "marker-typed map flows through DomainLoadStoreParameter");

        // custom (non-Password/CallbackHandler) implementations are rejected by
        // the engine when recovering key entries — UnsupportedOperationException
        java.security.KeyStore jce = java.security.KeyStore.getInstance("JCEKS");
        jce.load(null, null);
        jce.setKeyEntry("s", new javax.crypto.spec.SecretKeySpec(new byte[16], "AES"),
                "pw".toCharArray(), null);
        boolean customRejected = false;
        try { jce.getEntry("s", anon); }
        catch (UnsupportedOperationException uoe) { customRejected = true; }
        check(customRejected, "custom ProtectionParameter rejected by getEntry");

        // DomainLoadStoreParameter: snapshot + unmodifiable view of the marker-typed map
        dmap.clear();
        check(d.getProtectionParams().size() == 1, "protection params defensive copy");
        boolean putRejected = false;
        try { d.getProtectionParams().put("x", pw); }
        catch (UnsupportedOperationException uoe) { putRejected = true; }
        check(putRejected, "protection params view unmodifiable");

        // PasswordProtection destroy is idempotent through the concrete type
        destroyable.destroy();
        check(destroyable.isDestroyed(), "second destroy stays destroyed");
    }

    static class FauxCert extends java.security.cert.Certificate {
        FauxCert() { super("X.509"); }
        public byte[] getEncoded() { return new byte[]{1, 2, 3}; }
        public void verify(java.security.PublicKey key) { }
        public void verify(java.security.PublicKey key, String sigProvider) { }
        public String toString() { return "FauxCert"; }
        public java.security.PublicKey getPublicKey() { return new FauxKey(); }
    }

    static class FauxKey implements java.security.PublicKey {
        public String getAlgorithm() { return "RSA"; }
        public String getFormat() { return "X.509"; }
        public byte[] getEncoded() { return new byte[]{1, 2, 3}; }
    }

    static class FauxPrivateKey implements java.security.PrivateKey {
        public String getAlgorithm() { return "RSA"; }
        public String getFormat() { return "PKCS#8"; }
        public byte[] getEncoded() { return new byte[]{1, 2, 3}; }
    }
}
