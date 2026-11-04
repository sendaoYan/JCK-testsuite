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
        System.out.println("PASS (" + passed + " assertions)");
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
