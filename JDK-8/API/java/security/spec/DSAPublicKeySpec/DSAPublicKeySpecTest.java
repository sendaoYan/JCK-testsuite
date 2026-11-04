/*
 * @test
 * @summary java.security.spec.DSAPublicKeySpec conformance: constructor and getters.
 * @run main DSAPublicKeySpecTest
 */

public class DSAPublicKeySpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.math.BigInteger y = new java.math.BigInteger("2");
        java.math.BigInteger p = new java.math.BigInteger("11");
        java.math.BigInteger q = new java.math.BigInteger("5");
        java.math.BigInteger g = new java.math.BigInteger("2");
        java.security.spec.DSAPublicKeySpec s = new java.security.spec.DSAPublicKeySpec(y, p, q, g);
        check(y.equals(s.getY()) && p.equals(s.getP()) && q.equals(s.getQ()) && g.equals(s.getG()), "getY/getP/getQ/getG");
        // ---- 契约深化：null 语义/引用保持/值语义/KeyFactory 闭环 ----
        java.math.BigInteger dy = java.math.BigInteger.valueOf(11);
        java.math.BigInteger dp = java.math.BigInteger.valueOf(101);
        java.math.BigInteger dq = java.math.BigInteger.valueOf(103);
        java.math.BigInteger dg = java.math.BigInteger.valueOf(107);
        java.security.spec.DSAPublicKeySpec dks = new java.security.spec.DSAPublicKeySpec(dy, dp, dq, dg);
        check(dks.getY() == dy, "getY returns the same BigInteger reference");
        check(dks.getP() == dp, "getP returns the same BigInteger reference");
        check(dks.getQ() == dq, "getQ returns the same BigInteger reference");
        check(dks.getG() == dg, "getG returns the same BigInteger reference");
        java.security.spec.DSAPublicKeySpec dksNull = new java.security.spec.DSAPublicKeySpec(null, null, null, null);
        check(dksNull.getY() == null, "null-args ctor: getY null");
        check(dksNull.getP() == null, "null-args ctor: getP null");
        check(dksNull.getQ() == null, "null-args ctor: getQ null");
        check(dksNull.getG() == null, "null-args ctor: getG null");
        check(dks instanceof java.security.spec.KeySpec, "implements KeySpec");
        check(java.security.spec.KeySpec.class.isAssignableFrom(java.security.spec.DSAPublicKeySpec.class), "isAssignableFrom KeySpec");
        check(!dks.equals(new java.security.spec.DSAPublicKeySpec(dy, dp, dq, dg)), "no equals override: identity semantics");
        check(dks.equals(dks), "self-equality");
        check(dks.hashCode() == dks.hashCode(), "hashCode stable");
        check(dks.toString().contains("DSAPublicKeySpec"), "default toString contains class name");
        check(!(dks instanceof java.io.Serializable), "not Serializable");
        try {
            java.security.KeyPairGenerator kpg = java.security.KeyPairGenerator.getInstance("DSA");
            kpg.initialize(1024);
            java.security.KeyPair kp = kpg.generateKeyPair();
            java.security.KeyFactory kf = java.security.KeyFactory.getInstance("DSA");
            java.security.spec.DSAPublicKeySpec fromKey = kf.getKeySpec(kp.getPublic(), java.security.spec.DSAPublicKeySpec.class);
            check(fromKey.getY() != null, "KeyFactory.getKeySpec(DSA public): y non-null");
            check(fromKey.getP() != null, "KeyFactory.getKeySpec(DSA public): p non-null");
            check(fromKey.getQ() != null, "KeyFactory.getKeySpec(DSA public): q non-null");
            check(fromKey.getG() != null, "KeyFactory.getKeySpec(DSA public): g non-null");
            check(fromKey.getP().bitLength() == 1024, "spec p length matches initialized key size");
            java.security.PublicKey restored = kf.generatePublic(fromKey);
            check(restored.equals(kp.getPublic()), "round-trip: spec -> generatePublic restores the same key");
        } catch (Exception e) {
            throw new AssertionError("KeyFactory interop failed: " + e, e);
        }
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
