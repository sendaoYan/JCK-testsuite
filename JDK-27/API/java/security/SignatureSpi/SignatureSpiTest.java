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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract deepening helpers ----
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

    static class FakeKey implements java.security.PrivateKey, java.security.PublicKey {
        public String getAlgorithm() { return "Fake"; }
        public String getFormat() { return "RAW"; }
        public byte[] getEncoded() { return new byte[]{1}; }
    }

    static class RecSpi extends java.security.SignatureSpi {
        java.security.Key lastInitKey; int initSignCalls, initVerifyCalls, arrCalls, byteCalls, signCalls, verifyCalls, setParamCalls, getParamCalls;
        byte[] lastVerifyArg; int lastOff = -1, lastLen = -1;
        protected void engineInitVerify(java.security.PublicKey k) { initVerifyCalls++; lastInitKey = k; }
        protected void engineInitSign(java.security.PrivateKey k) { initSignCalls++; lastInitKey = k; }
        protected void engineUpdate(byte b) { byteCalls++; }
        protected void engineUpdate(byte[] b, int off, int len) { arrCalls++; lastOff = off; lastLen = len; }
        protected byte[] engineSign() { signCalls++; return new byte[]{(byte)1,(byte)2,(byte)3}; }
        protected boolean engineVerify(byte[] sig) { verifyCalls++; lastVerifyArg = sig; return sig.length == 3 && sig[0] == 1 && sig[1] == 2 && sig[2] == 3; }
        @SuppressWarnings("deprecation")
        protected void engineSetParameter(String p, Object v) { setParamCalls++; }
        @SuppressWarnings("deprecation")
        protected Object engineGetParameter(String p) { getParamCalls++; return "param"; }
        void doInitSign(java.security.PrivateKey k, java.security.SecureRandom r) throws java.security.InvalidKeyException { engineInitSign(k, r); }
        int doSign(byte[] b, int o, int l) throws java.security.SignatureException { return engineSign(b, o, l); }
        boolean doVerify(byte[] s, int o, int l) throws java.security.SignatureException { return engineVerify(s, o, l); }
        void doUpdate(java.nio.ByteBuffer b) { engineUpdate(b); }
        void doSetParam(java.security.spec.AlgorithmParameterSpec s) throws java.security.InvalidAlgorithmParameterException { engineSetParameter(s); }
        java.security.AlgorithmParameters doGetParams() { return engineGetParameters(); }
        java.security.SecureRandom getAppRandom() { return appRandom; }
        Object doClone() throws CloneNotSupportedException { return clone(); }
    }

    static void testContractDeep() throws Exception {
        Class<?> c = java.security.SignatureSpi.class;

        // ---- structure: protected field + exact abstract/default split ----
        java.lang.reflect.Field f = c.getDeclaredField("appRandom");
        check(java.lang.reflect.Modifier.isProtected(f.getModifiers())
                && f.getType() == java.security.SecureRandom.class, "appRandom protected SecureRandom field");
        java.lang.reflect.Method m;
        m = c.getDeclaredMethod("engineInitVerify", java.security.PublicKey.class);
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()) && java.lang.reflect.Modifier.isProtected(m.getModifiers()), "engineInitVerify abstract+protected");
        m = c.getDeclaredMethod("engineInitSign", java.security.PrivateKey.class);
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineInitSign(priv) abstract");
        m = c.getDeclaredMethod("engineInitSign", java.security.PrivateKey.class, java.security.SecureRandom.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()) && java.lang.reflect.Modifier.isProtected(m.getModifiers()), "engineInitSign(priv,random) has default impl");
        m = c.getDeclaredMethod("engineUpdate", java.nio.ByteBuffer.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineUpdate(ByteBuffer) has default impl");
        m = c.getDeclaredMethod("engineSign", byte[].class, int.class, int.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineSign(byte[],int,int) has default impl");
        m = c.getDeclaredMethod("engineVerify", byte[].class, int.class, int.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineVerify(byte[],int,int) has default impl");
        m = c.getDeclaredMethod("engineSetParameter", java.security.spec.AlgorithmParameterSpec.class);
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineSetParameter(spec) has default impl");
        m = c.getDeclaredMethod("engineGetParameters");
        check(!java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "engineGetParameters has default impl");

        // ---- appRandom + engineInitSign(priv, random) default: stores then delegates ----
        RecSpi s = new RecSpi();
        check(s.getAppRandom() == null, "appRandom initially null");
        java.security.SecureRandom rnd = new java.security.SecureRandom(new byte[]{1});
        FakeKey fk = new FakeKey();
        s.doInitSign(fk, rnd);
        check(s.getAppRandom() == rnd, "engineInitSign(priv,random) stores appRandom");
        check(s.initSignCalls == 1 && s.lastInitKey == fk, "engineInitSign(priv,random) delegates to engineInitSign(priv)");
        RecSpi s0 = new RecSpi();
        s0.doInitSign(fk, null);
        check(s0.initSignCalls == 1 && s0.getAppRandom() == null, "engineInitSign(priv,null) delegates, appRandom stays null");

        // ---- engineSign(buf,off,len) default: copies engineSign() result ----
        byte[] out = new byte[8];
        int n = s.doSign(out, 2, 8);
        check(n == 3, "default engineSign returns signature length");
        check(out[2] == 1 && out[4] == 3 && out[5] == 0, "default engineSign copies at offset only");
        check(s.signCalls == 1, "default engineSign delegates to engineSign()");
        ctMsg(java.security.SignatureException.class, "partial signatures not returned",
                () -> s.doSign(new byte[8], 0, 2), "engineSign len < sigLength");
        ctMsg(java.security.SignatureException.class, "insufficient space in the output buffer to store the signature",
                () -> s.doSign(new byte[2], 0, 8), "engineSign buf too small");

        // ---- engineVerify(sig,off,len) default: slices then delegates ----
        check(s.doVerify(new byte[]{9, 1, 2, 3, 9}, 1, 3), "engineVerify(off,len) true for correct slice");
        check(s.lastVerifyArg != null && s.lastVerifyArg.length == 3 && s.lastVerifyArg[0] == 1,
                "engineVerify(off,len) delegates with sliced array");
        check(!s.doVerify(new byte[]{9, 9, 9}, 0, 3), "engineVerify(off,len) false for wrong sig");

        // ---- engineUpdate(ByteBuffer) default: bulk path ----
        RecSpi s2 = new RecSpi();
        java.nio.ByteBuffer heap = java.nio.ByteBuffer.wrap(new byte[]{4, 5});
        s2.doUpdate(heap);
        check(s2.arrCalls == 1 && s2.byteCalls == 0, "engineUpdate(ByteBuffer) bulk-delegates");
        check(s2.lastOff == 0 && s2.lastLen == 2 && heap.position() == 2, "engineUpdate(ByteBuffer) off/len/position");
        java.nio.ByteBuffer direct = java.nio.ByteBuffer.allocateDirect(4);
        direct.put(new byte[]{6}).flip();
        RecSpi s3 = new RecSpi();
        s3.doUpdate(direct);
        check(s3.arrCalls == 1 && s3.byteCalls == 0 && direct.position() == 1, "engineUpdate(direct ByteBuffer) bulk too");

        // ---- unsupported-parameter defaults ----
        ct(UnsupportedOperationException.class,
                () -> s.doSetParam(new java.security.spec.PSSParameterSpec("SHA-256", "MGF1", null, 32, 1)),
                "engineSetParameter(spec) default throws UOE");
        ct(UnsupportedOperationException.class, () -> s.doGetParams(), "engineGetParameters() default throws UOE");

        // ---- clone default: CNSE for non-Cloneable subclass ----
        ct(CloneNotSupportedException.class, () -> new RecSpi().doClone(), "clone default throws CNSE");

        // ---- facade sign/verify closed loop drives the SPI layer ----
        java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("DSA");
        kpg.initialize(1024);
        java.security.KeyPair kp = kpg.generateKeyPair();
        java.security.Signature sig = java.security.Signature.getInstance("SHA256withDSA");
        sig.initSign(kp.getPrivate());
        byte[] msg = "spi-facade-loop".getBytes("UTF-8");
        sig.update(msg);
        byte[] sigBytes = sig.sign();
        check(sigBytes != null && sigBytes.length > 0, "facade produced a signature");
        sig.initVerify(kp.getPublic());
        sig.update(msg);
        check(sig.verify(sigBytes), "facade verify true for untampered signature");
        sig.update("other".getBytes("UTF-8"));
        check(!sig.verify(sigBytes), "facade verify false for different message");
        // Corrupting the ASN.1 SEQUENCE header byte breaks DER decoding before the
        // cryptographic check: the facade reports SignatureException, not verify=false.
        sigBytes[0] ^= 0x01;
        sig.update(msg);
        boolean threw = false;
        try { sig.verify(sigBytes); } catch (java.security.SignatureException se) { threw = true; }
        check(threw, "facade SignatureException for signature with broken DER encoding");
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
