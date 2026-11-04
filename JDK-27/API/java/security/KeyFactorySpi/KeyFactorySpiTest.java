/*
 * @test
 * @summary java.security.KeyFactorySpi conformance: white-box structure + black-box behavior.
 * @run main KeyFactorySpiTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Key;
import java.security.KeyFactorySpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class KeyFactorySpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyFactorySpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkMethods(c, new String[]{"engineGeneratePublic", "engineGeneratePrivate", "engineGetKeySpec", "engineTranslateKey"});
    }

    static void testBlackBox() throws Exception {
        MyKfSpi f = new MyKfSpi();
        PublicKey pub = f.doGeneratePublic(new X509EncodedKeySpec(new byte[]{1, 2, 3}));
        check(pub != null, "engineGeneratePublic non-null");
        check("Dummy".equals(pub.getAlgorithm()), "public algorithm");
        PrivateKey priv = f.doGeneratePrivate(new PKCS8EncodedKeySpec(new byte[]{1, 2, 3}));
        check(priv != null, "engineGeneratePrivate non-null");
        check("Dummy".equals(priv.getAlgorithm()), "private algorithm");
        Key translated = f.doTranslateKey(pub);
        check(translated == pub, "engineTranslateKey returns same key");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
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



    static class MyKfSpi extends KeyFactorySpi {
        protected PublicKey engineGeneratePublic(KeySpec keySpec) throws java.security.spec.InvalidKeySpecException { return new DummyPublicKey(); }
        protected PrivateKey engineGeneratePrivate(KeySpec keySpec) throws java.security.spec.InvalidKeySpecException { return new DummyPrivateKey(); }
        protected <T extends KeySpec> T engineGetKeySpec(Key key, Class<T> keySpec) throws java.security.spec.InvalidKeySpecException { throw new java.security.spec.InvalidKeySpecException(); }
        protected Key engineTranslateKey(Key key) throws java.security.InvalidKeyException { return key; }
        PublicKey doGeneratePublic(KeySpec ks) throws java.security.spec.InvalidKeySpecException { return engineGeneratePublic(ks); }
        PrivateKey doGeneratePrivate(KeySpec ks) throws java.security.spec.InvalidKeySpecException { return engineGeneratePrivate(ks); }
        Key doTranslateKey(Key key) throws java.security.InvalidKeyException { return engineTranslateKey(key); }
    }

    static class DummyPublicKey implements PublicKey {
        public String getAlgorithm() { return "Dummy"; }
        public String getFormat() { return "Dummy"; }
        public byte[] getEncoded() { return new byte[]{1, 2, 3}; }
    }

    static class DummyPrivateKey implements PrivateKey {
        public String getAlgorithm() { return "Dummy"; }
        public String getFormat() { return "Dummy"; }
        public byte[] getEncoded() { return new byte[]{1, 2, 3}; }
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}
