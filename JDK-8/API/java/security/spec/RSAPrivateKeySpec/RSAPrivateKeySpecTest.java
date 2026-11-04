/*
 * @test
 * @summary java.security.spec.RSAPrivateKeySpec conformance: constructor and getters.
 * @run main RSAPrivateKeySpecTest
 */

public class RSAPrivateKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.spec.RSAPrivateKeySpec s = new java.security.spec.RSAPrivateKeySpec(
            java.math.BigInteger.valueOf(35), java.math.BigInteger.valueOf(5));
        check(java.math.BigInteger.valueOf(35).equals(s.getModulus()), "getModulus");
        check(java.math.BigInteger.valueOf(5).equals(s.getPrivateExponent()), "getPrivateExponent");
        // ---- 契约深化：构造变体/引用保持/值语义/KeyFactory 闭环 ----
        java.math.BigInteger pm = java.math.BigInteger.valueOf(35);
        java.math.BigInteger pe = java.math.BigInteger.valueOf(5);
        java.security.spec.RSAPrivateKeySpec rp = new java.security.spec.RSAPrivateKeySpec(pm, pe);
        check(rp.getModulus() == pm, "getModulus returns the same BigInteger reference");
        check(rp.getPrivateExponent() == pe, "getPrivateExponent returns the same BigInteger reference");
        check(rp.getParams() == null, "2-arg ctor leaves params null");
        java.security.spec.MGF1ParameterSpec rpParams = new java.security.spec.MGF1ParameterSpec("SHA-256");
        check(new java.security.spec.RSAPrivateKeySpec(pm, pe, rpParams).getParams() == rpParams,
                "3-arg ctor params passthrough");
        check(new java.security.spec.RSAPrivateKeySpec(null, null).getModulus() == null, "null-args ctor: modulus null");
        check(new java.security.spec.RSAPrivateKeySpec(null, null).getPrivateExponent() == null, "null-args ctor: exponent null");
        check(rp instanceof java.security.spec.KeySpec, "implements KeySpec");
        check(java.security.spec.KeySpec.class.isAssignableFrom(java.security.spec.RSAPrivateKeySpec.class), "isAssignableFrom KeySpec");
        check(java.security.spec.RSAPrivateKeySpec.class.isAssignableFrom(java.security.spec.RSAPrivateCrtKeySpec.class),
                "RSAPrivateCrtKeySpec is a RSAPrivateKeySpec");
        check(!rp.equals(new java.security.spec.RSAPrivateKeySpec(pm, pe)), "no equals override: identity semantics");
        check(rp.hashCode() == rp.hashCode(), "hashCode stable");
        check(rp.toString().contains("RSAPrivateKeySpec"), "default toString contains class name");
        check(!(rp instanceof java.io.Serializable), "not Serializable");
        try {
            java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("RSA");
            kpg.initialize(512);
            java.security.KeyPair kp = kpg.generateKeyPair();
            java.security.KeyFactory kf = java.security.KeyFactory.getInstance("RSA");
            java.security.spec.RSAPrivateKeySpec fromKey = kf.getKeySpec(kp.getPrivate(), java.security.spec.RSAPrivateKeySpec.class);
            check(fromKey.getModulus().bitLength() == 512, "spec modulus bit length matches generated key");
            java.security.PrivateKey restored = kf.generatePrivate(fromKey);
            check(restored.equals(kp.getPrivate()), "round-trip: spec -> generatePrivate restores the same key");
        } catch (Exception e) {
            throw new AssertionError("KeyFactory interop failed: " + e, e);
        }
        check(new java.security.spec.RSAPrivateKeySpec(pm, pe, null).getParams() == null,
                "3-arg ctor with null params keeps it null");
        check(!java.lang.reflect.Modifier.isFinal(java.security.spec.RSAPrivateKeySpec.class.getModifiers()),
                "non-final: RSAPrivateCrtKeySpec extends it");
        check(java.security.spec.RSAPrivateKeySpec.class.getDeclaredConstructors().length == 2,
                "exactly two public constructors (2-arg and 3-arg)");
        check(rp instanceof java.security.spec.KeySpec, "instanceof KeySpec");
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- helpers for contract deepening (appended) ----
    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r, String sub, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                check(t.getMessage() != null && t.getMessage().contains(sub),
                        msg + ": message contains \"" + sub + "\" (actual: " + t.getMessage() + ")");
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName() + " but nothing was thrown");
    }

    static byte[] serializeToBytes(Object o) {
        try {
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            new java.io.ObjectOutputStream(bos).writeObject(o);
            return bos.toByteArray();
        } catch (java.io.IOException e) {
            throw new AssertionError("serialize failed: " + e, e);
        }
    }

    static Object deserializeFromBytes(byte[] b) {
        try {
            return new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(b)).readObject();
        } catch (Exception e) {
            throw new AssertionError("deserialize failed: " + e, e);
        }
    }
}
