/*
 * @test
 * @summary java.security.KeyFactorySpi conformance: abstract class hierarchy and key methods.
 * @run main KeyFactorySpiTest
 */

public class KeyFactorySpiTest {
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

    static class FakeKey implements java.security.PrivateKey, java.security.PublicKey {
        public String getAlgorithm() { return "Fake"; }
        public String getFormat() { return "RAW"; }
        public byte[] getEncoded() { return new byte[]{1}; }
    }

    // Pass-through recording subclass: all four engine methods are abstract, the SPI class
    // itself supplies no default implementations to inherit.
    static class RecSpi extends java.security.KeyFactorySpi {
        java.security.spec.KeySpec lastGenPubSpec, lastGenPrivSpec, lastReturnedSpec;
        java.security.Key lastKey, lastTranslated;
        Class<?> lastSpecClass;
        protected java.security.PublicKey engineGeneratePublic(java.security.spec.KeySpec spec) {
            lastGenPubSpec = spec; return (java.security.PublicKey) lastKey;
        }
        protected java.security.PrivateKey engineGeneratePrivate(java.security.spec.KeySpec spec) {
            lastGenPrivSpec = spec; return (java.security.PrivateKey) lastKey;
        }
        @SuppressWarnings("unchecked")
        protected <T extends java.security.spec.KeySpec> T engineGetKeySpec(java.security.Key key, Class<T> specClass) {
            lastKey = key; lastSpecClass = specClass; return (T) lastReturnedSpec;
        }
        protected java.security.Key engineTranslateKey(java.security.Key key) { lastTranslated = key; return key; }
        java.security.PublicKey doGenPub(java.security.spec.KeySpec s) throws java.security.spec.InvalidKeySpecException { return engineGeneratePublic(s); }
        java.security.PrivateKey doGenPriv(java.security.spec.KeySpec s) throws java.security.spec.InvalidKeySpecException { return engineGeneratePrivate(s); }
        java.security.Key doTranslate(java.security.Key k) throws java.security.InvalidKeyException { return engineTranslateKey(k); }
        <T extends java.security.spec.KeySpec> T doGetKeySpec(java.security.Key k, Class<T> cls) { return engineGetKeySpec(k, cls); }
    }

    static void testContractDeep() throws Exception {
        Class<?> c = java.security.KeyFactorySpi.class;

        // ---- structure: public no-arg ctor, four abstract protected engine methods, nothing else ----
        check(c.getConstructor() != null, "public no-arg constructor");
        java.lang.reflect.Method m;
        m = c.getDeclaredMethod("engineGeneratePublic", java.security.spec.KeySpec.class);
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()) && java.lang.reflect.Modifier.isProtected(m.getModifiers()), "engineGeneratePublic abstract+protected");
        m = c.getDeclaredMethod("engineGeneratePrivate", java.security.spec.KeySpec.class);
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineGeneratePrivate abstract");
        m = c.getDeclaredMethod("engineGetKeySpec", java.security.Key.class, Class.class);
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineGetKeySpec abstract");
        m = c.getDeclaredMethod("engineTranslateKey", java.security.Key.class);
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineTranslateKey abstract");
        int abstractCount = 0;
        for (java.lang.reflect.Method mm : c.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isAbstract(mm.getModifiers())) abstractCount++;
        check(abstractCount == 4, "exactly 4 declared methods, all abstract (no default engine impls)");

        // ---- pass-through: engine methods receive arguments verbatim ----
        RecSpi spi = new RecSpi();
        FakeKey fk = new FakeKey();
        spi.lastKey = fk;
        java.security.spec.X509EncodedKeySpec x509 = new java.security.spec.X509EncodedKeySpec(new byte[]{7, 7});
        check(spi.doGenPub(x509) == fk, "engineGeneratePublic returns subclass value");
        check(spi.lastGenPubSpec == x509, "engineGeneratePublic received exact spec");
        java.security.spec.PKCS8EncodedKeySpec pkcs8 = new java.security.spec.PKCS8EncodedKeySpec(new byte[]{8});
        check(spi.doGenPriv(pkcs8) == fk, "engineGeneratePrivate returns subclass value");
        check(spi.lastGenPrivSpec == pkcs8, "engineGeneratePrivate received exact spec");
        java.security.spec.DSAPublicKeySpec dsaSpec = new java.security.spec.DSAPublicKeySpec(
                java.math.BigInteger.TEN, java.math.BigInteger.ONE, java.math.BigInteger.TEN, java.math.BigInteger.valueOf(4));
        spi.lastReturnedSpec = dsaSpec;
        check(spi.doGetKeySpec(fk, java.security.spec.DSAPublicKeySpec.class) == dsaSpec,
                "engineGetKeySpec returns subclass spec");
        check(spi.lastKey == fk && spi.lastSpecClass == java.security.spec.DSAPublicKeySpec.class,
                "engineGetKeySpec received exact key and class");
        check(spi.doTranslate(fk) == fk, "engineTranslateKey returns same key");
        check(spi.lastTranslated == fk, "engineTranslateKey received exact key");

        // ---- facade closed loop: KeyFactory drives a real SPI (DSA) ----
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        java.security.KeyPair kp = kpg.generateKeyPair();
        java.security.KeyFactory kf = java.security.KeyFactory.getInstance("DSA");
        java.security.spec.X509EncodedKeySpec pubSpec = kf.getKeySpec(kp.getPublic(), java.security.spec.X509EncodedKeySpec.class);
        check("X.509".equals(pubSpec.getFormat()), "facade getKeySpec format X.509");
        check(java.util.Arrays.equals(pubSpec.getEncoded(), kp.getPublic().getEncoded()), "spec bytes equal key encoding");
        java.security.PublicKey pubBack = kf.generatePublic(pubSpec);
        check(pubBack.equals(kp.getPublic()), "facade generatePublic round-trip equals");
        java.security.spec.PKCS8EncodedKeySpec privSpec = kf.getKeySpec(kp.getPrivate(), java.security.spec.PKCS8EncodedKeySpec.class);
        check("PKCS#8".equals(privSpec.getFormat()), "facade getKeySpec format PKCS#8");
        java.security.PrivateKey privBack = kf.generatePrivate(privSpec);
        check(privBack.equals(kp.getPrivate()), "facade generatePrivate round-trip equals");
        java.security.Key translated = kf.translateKey(kp.getPublic());
        check(translated.equals(kp.getPublic()), "facade translateKey(pub) equals");

        // ---- facade null/invalid matrix ----
        ct(java.security.spec.InvalidKeySpecException.class, () -> kf.generatePublic(null), "facade generatePublic(null) -> InvalidKeySpecException");
        ct(java.security.spec.InvalidKeySpecException.class, () -> kf.generatePrivate(null), "facade generatePrivate(null) -> InvalidKeySpecException");
        ct(java.security.spec.InvalidKeySpecException.class, () -> kf.getKeySpec(null, java.security.spec.X509EncodedKeySpec.class), "facade getKeySpec(null key) -> InvalidKeySpecException");
        ct(NullPointerException.class, () -> kf.getKeySpec(kp.getPublic(), null), "facade getKeySpec(key, null) -> NPE");
        ct(java.security.InvalidKeyException.class, () -> kf.translateKey(null), "facade translateKey(null) -> InvalidKeyException");
        ct(java.security.spec.InvalidKeySpecException.class,
                () -> kf.generatePublic(new java.security.spec.PKCS8EncodedKeySpec(new byte[]{1})),
                "facade DSA generatePublic(PKCS8 spec) wrong spec type -> InvalidKeySpecException");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.KeyFactorySpi");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) && !c.isInterface(), "is abstract class");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("engineGeneratePrivate"), "declares engineGeneratePrivate");
        check(ms.contains("engineGeneratePublic"), "declares engineGeneratePublic");
        check(ms.contains("engineGetKeySpec"), "declares engineGetKeySpec");
        check(ms.contains("engineTranslateKey"), "declares engineTranslateKey");

        // ---- contract deepening: pass-through SPI + facade round-trip/null matrix ----
        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }
}
