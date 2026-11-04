/*
 * @test
 * @summary java.security.KeyPairGeneratorSpi conformance: white-box structure + black-box behavior.
 * @run main KeyPairGeneratorSpiTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.KeyPair;
import java.security.KeyPairGeneratorSpi;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class KeyPairGeneratorSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.KeyPairGeneratorSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkMethods(c, new String[]{"generateKeyPair", "initialize"});
    }

    static void testBlackBox() throws Exception {
        MyKpgSpi g = new MyKpgSpi();
        g.doInitialize(1024, null);
        KeyPair kp = g.doGenerateKeyPair();
        check(kp != null, "generateKeyPair non-null");
        check(kp.getPublic() != null, "public key non-null");
        check(kp.getPrivate() != null, "private key non-null");
        check("Dummy".equals(kp.getPublic().getAlgorithm()), "public algorithm");
        check("Dummy".equals(kp.getPublic().getFormat()), "public format");
        check(Arrays.equals(kp.getPublic().getEncoded(), new byte[]{1, 2, 3}), "public encoded");
        check("Dummy".equals(kp.getPrivate().getAlgorithm()), "private algorithm");
        checkThrows(UnsupportedOperationException.class, () -> g.doInitialize(new DummySpec(), null));
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static class MyKpgSpi extends KeyPairGeneratorSpi {
        public void initialize(int keysize, SecureRandom random) { }
        public KeyPair generateKeyPair() { return new KeyPair(new DummyPublicKey(), new DummyPrivateKey()); }
        void doInitialize(int keysize, SecureRandom random) { initialize(keysize, random); }
        void doInitialize(AlgorithmParameterSpec p, SecureRandom r) {
            try { initialize(p, r); } catch (java.security.InvalidAlgorithmParameterException e) { throw new RuntimeException(e); }
        }
        KeyPair doGenerateKeyPair() { return generateKeyPair(); }
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

    static class DummySpec implements AlgorithmParameterSpec { }

    static void checkThrows(Class<? extends Throwable> type, Runnable r) {
        try { r.run(); } catch (Throwable t) { check(type.isInstance(t), "throws " + type.getSimpleName()); return; }
        throw new AssertionError("expected " + type.getSimpleName());
    }

    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static void checkInterfaces(Class<?> c, String[] names) { for (String n : names) check(hasInterface(c, n), "implements " + n); }
    static void checkMethods(Class<?> c, String[] names) { for (String n : names) check(hasMethod(c, n), "method " + n); }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static void checkEnumConstants(Class<?> c, String[] names) { Object[] cs = c.getEnumConstants(); check(cs != null, "enum constants"); check(cs.length == names.length, "enum constant count"); for (String n : names) check(hasConst(cs, n), "constant " + n); }
    static boolean hasConst(Object[] cs, String name) { for (Object o : cs) if (((Enum<?>) o).name().equals(name)) return true; return false; }
}
