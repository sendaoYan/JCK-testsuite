/*
 * @test
 * @summary java.security.SignatureSpi conformance: white-box structure + black-box behavior.
 * @run main SignatureSpiTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AlgorithmParameters;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.SignatureSpi;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public class SignatureSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.security.SignatureSpi");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        checkMethods(c, new String[]{"clone"});
    }

    static void testBlackBox() throws Exception {
        MySigSpi s = new MySigSpi();
        check(s.getAppRandom() == null, "appRandom initially null");
        SecureRandom r = new SecureRandom();
        s.doInitSign(new DummyPrivateKey(), r);
        check(s.getAppRandom() == r, "engineInitSign stores appRandom");
        byte[] out = new byte[8];
        int n = s.doSign(out, 1, 8);
        check(n == 3, "inherited engineSign returns length");
        check(out[1] == 4 && out[2] == 5 && out[3] == 6, "signature bytes written");
        check(s.doVerify(new byte[]{4, 5, 6}, 0, 3), "engineVerify true");
        check(!s.doVerify(new byte[]{9, 9, 9}, 0, 3), "engineVerify false");
        checkThrows(UnsupportedOperationException.class, () -> s.doSetParameter(new DummySpec()));
        checkThrows(UnsupportedOperationException.class, () -> s.doGetParameters());
        Object c = s.clone();
        check(c != null && c != s, "clone distinct instance");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static class MySigSpi extends SignatureSpi implements Cloneable {
        protected void engineInitVerify(PublicKey publicKey) throws java.security.InvalidKeyException { }
        protected void engineInitSign(PrivateKey privateKey) throws java.security.InvalidKeyException { }
        protected void engineUpdate(byte b) throws java.security.SignatureException { }
        protected void engineUpdate(byte[] b, int off, int len) throws java.security.SignatureException { }
        protected byte[] engineSign() throws java.security.SignatureException { return new byte[]{4, 5, 6}; }
        protected boolean engineVerify(byte[] sigBytes) throws java.security.SignatureException { return Arrays.equals(sigBytes, new byte[]{4, 5, 6}); }
        protected void engineSetParameter(String param, Object value) throws java.security.InvalidParameterException { }
        protected Object engineGetParameter(String param) throws java.security.InvalidParameterException { return null; }
        void doInitSign(PrivateKey pk, SecureRandom r) throws java.security.InvalidKeyException { engineInitSign(pk, r); }
        int doSign(byte[] out, int off, int len) throws java.security.SignatureException { return engineSign(out, off, len); }
        boolean doVerify(byte[] sig, int off, int len) throws java.security.SignatureException { return engineVerify(sig, off, len); }
        void doSetParameter(AlgorithmParameterSpec p) {
            try { engineSetParameter(p); } catch (java.security.InvalidAlgorithmParameterException e) { throw new RuntimeException(e); }
        }
        AlgorithmParameters doGetParameters() { return engineGetParameters(); }
        SecureRandom getAppRandom() { return appRandom; }
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
