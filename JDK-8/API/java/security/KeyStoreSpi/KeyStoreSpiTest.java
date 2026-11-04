/*
 * @test
 * @summary java.security.KeyStoreSpi conformance: abstract class hierarchy and key methods.
 * @run main KeyStoreSpiTest
 */

public class KeyStoreSpiTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface TR { void run() throws Throwable; }

    static void ct(Class<? extends Throwable> expected, TR r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static void ctMsg(Class<? extends Throwable> expected, String frag, TR r, String msg) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t) && t.getMessage() != null && t.getMessage().contains(frag)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + "(" + frag + ") but got "
                    + t.getClass().getName() + "(" + t.getMessage() + ")", t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + "(" + frag + ") but nothing was thrown");
    }

    static class FakeKey implements java.security.PrivateKey {
        public String getAlgorithm() { return "Fake"; }
        public String getFormat() { return "RAW"; }
        public byte[] getEncoded() { return new byte[]{1}; }
    }

    static class FakePublicKey implements java.security.PublicKey {
        public String getAlgorithm() { return "Fake"; }
        public String getFormat() { return "X.509"; }
        public byte[] getEncoded() { return new byte[]{2}; }
    }

    // Minimal Certificate the JDK-default entry adapters can chew on (chain building calls getPublicKey()).
    static class FakeCertificate extends java.security.cert.Certificate {
        FakeCertificate() { super("Fake"); }
        public byte[] getEncoded() { return new byte[]{3}; }
        public java.security.PublicKey getPublicKey() { return new FakePublicKey(); }
        public String toString() { return "FakeCertificate"; }
        public void verify(java.security.PublicKey key) { }
        public void verify(java.security.PublicKey key, String sigProvider) { }
    }

    static javax.crypto.SecretKey fakeSecretKey() {
        return new javax.crypto.SecretKey() {
            public String getAlgorithm() { return "FakeAES"; }
            public String getFormat() { return "RAW"; }
            public byte[] getEncoded() { return new byte[16]; }
        };
    }

    // Records every call; does NOT override engineStore(param)/engineLoad(param)/
    // engineGetEntry/engineSetEntry/engineEntryInstanceOf so the JDK defaults are exercised.
    static class DefaultsSpi extends java.security.KeyStoreSpi {
        final java.util.Map<String, java.security.Key> keys = new java.util.LinkedHashMap<String, java.security.Key>();
        final java.util.Map<String, java.security.cert.Certificate> certs = new java.util.LinkedHashMap<String, java.security.cert.Certificate>();
        int loadCalls; java.io.InputStream lastStream; char[] lastPass; java.security.Key lastSetKey; char[] lastSetPass;
        java.security.cert.Certificate[] lastSetChain; String lastCertAlias; java.security.cert.Certificate lastSetCert;
        public java.security.Key engineGetKey(String alias, char[] password) { return keys.get(alias); }
        public java.security.cert.Certificate[] engineGetCertificateChain(String alias) { return null; }
        public java.security.cert.Certificate engineGetCertificate(String alias) { return certs.get(alias); }
        public java.util.Date engineGetCreationDate(String alias) { return alias == null ? null : new java.util.Date(0L); }
        public void engineSetKeyEntry(String alias, java.security.Key key, char[] password, java.security.cert.Certificate[] chain) {
            keys.put(alias, key); lastSetKey = key; lastSetPass = password; lastSetChain = chain;
        }
        public void engineSetKeyEntry(String alias, byte[] key, java.security.cert.Certificate[] chain) { }
        public void engineSetCertificateEntry(String alias, java.security.cert.Certificate cert) {
            certs.put(alias, cert); lastCertAlias = alias; lastSetCert = cert;
        }
        public void engineDeleteEntry(String alias) { keys.remove(alias); certs.remove(alias); }
        public java.util.Enumeration<String> engineAliases() { return java.util.Collections.enumeration(keys.keySet()); }
        public boolean engineContainsAlias(String alias) { return keys.containsKey(alias) || certs.containsKey(alias); }
        public int engineSize() { return keys.size() + certs.size(); }
        public boolean engineIsKeyEntry(String alias) { return keys.containsKey(alias); }
        public boolean engineIsCertificateEntry(String alias) { return certs.containsKey(alias); }
        public String engineGetCertificateAlias(java.security.cert.Certificate cert) { return null; }
        public void engineStore(java.io.OutputStream stream, char[] password) { }
        public void engineLoad(java.io.InputStream stream, char[] password) { loadCalls++; lastStream = stream; lastPass = password; }
        void doStoreParam(java.security.KeyStore.LoadStoreParameter p) throws Exception { engineStore(p); }
        void doLoadParam(java.security.KeyStore.LoadStoreParameter p) throws Exception { engineLoad(p); }
        java.security.KeyStore.Entry doGetEntry(String a, java.security.KeyStore.ProtectionParameter p) throws Exception { return engineGetEntry(a, p); }
        void doSetEntry(String a, java.security.KeyStore.Entry e, java.security.KeyStore.ProtectionParameter p) throws Exception { engineSetEntry(a, e, p); }
        boolean doEntryInstanceOf(String a, Class<? extends java.security.KeyStore.Entry> c) { return engineEntryInstanceOf(a, c); }
    }

    // Self-signed DSA certificate generated with keytool (embedded so the test is self-contained).
    static final String REAL_CERT_B64 =
        "MIICfzCCAjugAwIBAgIEfpsReTANBglghkgBZQMEAwIFADAPMQ0wCwYDVQQDEwRUZXN0MB4XDTI2MTEwNDIyMDIyNFoXDTI2MTEw" +
        "NTIyMDIyNFowDzENMAsGA1UEAxMEVGVzdDCCAbgwggEsBgcqhkjOOAQBMIIBHwKBgQD9f1OBHXUSKVLfSpwu7OTn9hG3UjzvRADD" +
        "Hj+AtlEmaUVdQCJR+1k9jVj6v8X1ujD2y5tVbNeBO4AdNG/yZmC3a5lQpaSfn+gEexAiwk+7qdf+t8Yb+DtX58aophUPBPuD9tPF" +
        "HsMCNVQTWhaRMvZ1864rYdcq7/IiAxmd0UgBxwIVAJdgUI8VIwvMspK5gqLrhAvwWBz1AoGBAPfhoIXWmz3ey7yrXDa4V7l5lK+7" +
        "+jrqgvlXTAs9B4JnUVlXjrrUWU/mcQcQgYC0SRZxI+hMKBYTt88JMozIpuE8FnqLVHyNKOCjrh4rs6Z1kW6jfwv6ITVi8ftiegEk" +
        "O8yk8b6oUZCJqIPf4VrlnwaSi2ZegHtVJWQBTDv+z0kqA4GFAAKBgQC4Xg/xDMXJc3PL6fTE5It+vEFdZmsy1ub2ssUnsMmWjgyA" +
        "j07/lmiHp6YxbxG6cIV3cKLWvJPZ/5HZjB/3QFbCGdvgDlt79eyDjMhPKX8UxGsTFaiwZMi8h1aaNEGfSMK5FfUIcn0ylb+75AjQ" +
        "UprAJ7bjkYskKopTzicYUO3RjqMhMB8wHQYDVR0OBBYEFNA09HbCMDogcgcxtaYyMORhaE7vMA0GCWCGSAFlAwQDAgUAAy8AMCwC" +
        "FGRokEIiSUXDTTESM5SwbIgF43kQAhQFlePIpSnMK3rthEOz0HnkC1z9TQ==";

    static java.security.KeyStore.ProtectionParameter ppParam(String pw) {
        return new java.security.KeyStore.PasswordProtection(pw.toCharArray());
    }

    static java.security.KeyStore.LoadStoreParameter pwParam(final String pw) {
        return new java.security.KeyStore.LoadStoreParameter() {
            public java.security.KeyStore.ProtectionParameter getProtectionParameter() {
                return new java.security.KeyStore.PasswordProtection(pw.toCharArray());
            }
        };
    }

    static void testContractDeep() throws Exception {
        Class<?> c = java.security.KeyStoreSpi.class;

        // ---- structure: public class, 16 JDK-8 abstract engine methods, all public abstract ----
        check(c.getConstructor() != null, "public no-arg constructor");
        int abstractCount = 0;
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) { abstractCount++; names.add(m.getName()); }
        }
        check(abstractCount == 16, "16 abstract engine methods, got " + abstractCount + ": " + names);
        java.lang.reflect.Method m = c.getDeclaredMethod("engineStore", java.security.KeyStore.LoadStoreParameter.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()) && java.lang.reflect.Modifier.isPublic(m.getModifiers()),
                "engineStore(LoadStoreParameter) public default");
        m = c.getDeclaredMethod("engineLoad", java.security.KeyStore.LoadStoreParameter.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineLoad(LoadStoreParameter) default");
        m = c.getDeclaredMethod("engineGetEntry", String.class, java.security.KeyStore.ProtectionParameter.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineGetEntry default");
        m = c.getDeclaredMethod("engineSetEntry", String.class, java.security.KeyStore.Entry.class, java.security.KeyStore.ProtectionParameter.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineSetEntry default");
        m = c.getDeclaredMethod("engineEntryInstanceOf", String.class, Class.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineEntryInstanceOf default");

        DefaultsSpi ks = new DefaultsSpi();
        FakeKey fk = new FakeKey();
        FakeCertificate fc = new FakeCertificate();

        // ---- engineStore(LoadStoreParameter) default: unsupported, also for null ----
        ct(UnsupportedOperationException.class, () -> ks.doStoreParam(null), "default engineStore(null param) -> UOE");
        ct(UnsupportedOperationException.class, () -> ks.doStoreParam(pwParam("p")), "default engineStore(param) -> UOE");

        // ---- engineLoad(LoadStoreParameter) default ----
        ks.doLoadParam(null);
        check(ks.loadCalls == 1 && ks.lastStream == null && ks.lastPass == null,
                "default engineLoad(null param) delegates engineLoad(null,null)");
        ct(UnsupportedOperationException.class, () -> ks.doLoadParam(pwParam("pw")),
                "JDK 8: default engineLoad(param with password) -> UOE (JDK 9+ delegates instead)");

        // ---- engineGetEntry defaults ----
        check(ks.doGetEntry("absent", ppParam("p")) == null, "default engineGetEntry absent alias -> null");
        ks.keys.put("k1", fk);
        ctMsg(NullPointerException.class, "invalid null input",
                () -> ks.doGetEntry("k1", ppParam("p")),
                "key entry + PasswordProtection builds PrivateKeyEntry (chain null -> NPE from entry ctor)");
        ct(NullPointerException.class, () -> ks.doGetEntry("k1", new java.security.KeyStore.CallbackHandlerProtection(null)),
                "key entry + CallbackHandlerProtection same NPE path");
        ct(java.security.UnrecoverableKeyException.class, () -> ks.doGetEntry("k1", null),
                "key entry + null protection -> UnrecoverableKeyException");
        ks.certs.put("c1", fc);
        java.security.KeyStore.Entry certEntry = ks.doGetEntry("c1", null);
        check(certEntry instanceof java.security.KeyStore.TrustedCertificateEntry
                && ((java.security.KeyStore.TrustedCertificateEntry) certEntry).getTrustedCertificate() == fc,
                "cert entry + null protection -> TrustedCertificateEntry with same cert");

        // ---- engineSetEntry defaults ----
        javax.crypto.SecretKey sk = fakeSecretKey();
        ks.doSetEntry("s1", new java.security.KeyStore.SecretKeyEntry(sk), ppParam("p"));
        check(ks.keys.get("s1") == sk, "default engineSetEntry(SecretKeyEntry) -> engineSetKeyEntry(key)");
        check(java.util.Arrays.equals(ks.lastSetPass, "p".toCharArray()), "default engineSetEntry passes password through");
        ks.doSetEntry("t1", new java.security.KeyStore.TrustedCertificateEntry(fc), null);
        check(ks.certs.get("t1") == fc && ks.lastCertAlias.equals("t1"),
                "default engineSetEntry(TrustedCertificateEntry, null prot) -> engineSetCertificateEntry");
        ks.doSetEntry("p1", new java.security.KeyStore.PrivateKeyEntry(fk, new java.security.cert.Certificate[]{fc}), ppParam("p"));
        check(ks.keys.get("p1") == fk && ks.lastSetChain != null && ks.lastSetChain.length == 1,
                "default engineSetEntry(PrivateKeyEntry) -> engineSetKeyEntry with chain");

        // ---- engineEntryInstanceOf defaults ----
        // "k1" is a key entry with a null chain: PrivateKeyEntry requires a chain, so it classifies as SecretKeyEntry.
        check(!ks.doEntryInstanceOf("k1", java.security.KeyStore.PrivateKeyEntry.class),
                "default engineEntryInstanceOf: key entry + null chain not PrivateKeyEntry");
        check(ks.doEntryInstanceOf("k1", java.security.KeyStore.SecretKeyEntry.class),
                "default engineEntryInstanceOf: key entry + null chain is SecretKeyEntry");
        check(ks.doEntryInstanceOf("t1", java.security.KeyStore.TrustedCertificateEntry.class),
                "default engineEntryInstanceOf: cert entry is TrustedCertificateEntry");
        check(!ks.doEntryInstanceOf("absent", java.security.KeyStore.TrustedCertificateEntry.class),
                "default engineEntryInstanceOf: absent alias false");

        // ---- facade loop: real JKS keystore SPI driven through the public KeyStore facade ----
        java.security.cert.Certificate realCert = java.security.cert.CertificateFactory.getInstance("X.509")
                .generateCertificate(new java.io.ByteArrayInputStream(
                        java.util.Base64.getDecoder().decode(REAL_CERT_B64)));
        check("X.509".equals(realCert.getType()) && "DSA".equals(realCert.getPublicKey().getAlgorithm()),
                "embedded keytool DSA certificate parses");
        java.security.KeyPair realKp = java.security.KeyPairGenerator.getInstance("DSA").genKeyPair();
        java.security.KeyStore jks = java.security.KeyStore.getInstance("JKS");
        jks.load(null, null);
        check(jks.size() == 0 && !jks.aliases().hasMoreElements(), "facade fresh JKS empty");
        jks.setKeyEntry("fk", realKp.getPrivate(), "pw".toCharArray(),
                new java.security.cert.Certificate[]{realCert});
        check(jks.isKeyEntry("fk"), "facade setKeyEntry classified as key entry");
        java.security.Key gotKey = jks.getKey("fk", "pw".toCharArray());
        check(gotKey != null && gotKey.equals(realKp.getPrivate()) && "PKCS#8".equals(gotKey.getFormat()),
                "facade getKey round-trips an equal PKCS#8 private key");
        ct(java.security.UnrecoverableKeyException.class,
                () -> jks.getKey("fk", "wrong".toCharArray()),
                "facade JKS getKey with wrong password -> UnrecoverableKeyException");
        check(jks.getCertificateChain("fk").length == 1
                && jks.getCertificateChain("fk")[0].equals(realCert), "facade chain preserved");
        check(jks.getCreationDate("fk") != null, "facade creationDate non-null");
        jks.setCertificateEntry("fc", realCert);
        check(jks.getCertificate("fc") == realCert && jks.isCertificateEntry("fc"), "facade cert entry same instance");
        check(jks.containsAlias("fk") && jks.containsAlias("fc") && jks.size() == 2, "facade size/contains");
        check(jks.containsAlias("FK"), "JKS aliases are case-insensitive (stored lower-case)");
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        jks.store(bos, "pw".toCharArray());
        check(bos.size() > 0, "facade store produced bytes");
        java.security.KeyStore jks2 = java.security.KeyStore.getInstance("JKS");
        jks2.load(new java.io.ByteArrayInputStream(bos.toByteArray()), "pw".toCharArray());
        check(jks2.size() == 2 && jks2.getKey("fk", "pw".toCharArray()).equals(realKp.getPrivate())
                && jks2.getCertificate("fc").equals(realCert),
                "facade store/load round-trip preserves key and cert entries");
        ct(java.io.IOException.class,
                () -> {
                    java.security.KeyStore bad = java.security.KeyStore.getInstance("JKS");
                    bad.load(new java.io.ByteArrayInputStream(bos.toByteArray()), "bad".toCharArray());
                },
                "facade JKS load with wrong password -> IOException");
        jks.deleteEntry("fc");
        check(jks.size() == 1 && !jks.containsAlias("fc"), "facade deleteEntry removes entry");
        java.security.KeyStore uninit = java.security.KeyStore.getInstance("JKS");
        ct(java.security.KeyStoreException.class, () -> uninit.size(), "facade size before load -> KeyStoreException");
        ct(java.security.KeyStoreException.class, () -> uninit.getKey("x", null), "facade getKey before load -> KeyStoreException");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.KeyStoreSpi");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("engineAliases"), "declares engineAliases");
        check(ms.contains("engineContainsAlias"), "declares engineContainsAlias");
        check(ms.contains("engineDeleteEntry"), "declares engineDeleteEntry");
        check(ms.contains("engineEntryInstanceOf"), "declares engineEntryInstanceOf");
        check(ms.contains("engineGetCertificate"), "declares engineGetCertificate");
        check(ms.contains("engineGetCertificateAlias"), "declares engineGetCertificateAlias");
        check(ms.contains("engineGetCertificateChain"), "declares engineGetCertificateChain");
        check(ms.contains("engineGetCreationDate"), "declares engineGetCreationDate");
        check(ms.contains("engineGetEntry"), "declares engineGetEntry");
        check(ms.contains("engineGetKey"), "declares engineGetKey");
        check(ms.contains("engineIsCertificateEntry"), "declares engineIsCertificateEntry");
        check(ms.contains("engineIsKeyEntry"), "declares engineIsKeyEntry");
        check(ms.contains("engineLoad"), "declares engineLoad");
        check(ms.contains("engineSetCertificateEntry"), "declares engineSetCertificateEntry");
        check(ms.contains("engineSetEntry"), "declares engineSetEntry");
        check(ms.contains("engineSetKeyEntry"), "declares engineSetKeyEntry");
        check(ms.contains("engineSize"), "declares engineSize");
        check(ms.contains("engineStore"), "declares engineStore");

        // ---- contract deepening: JDK default behaviors of the non-abstract engine methods + JKS facade loop ----
        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
