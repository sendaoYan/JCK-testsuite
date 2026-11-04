/*
 * @test
 * @summary java.security.spec.DSAParameterSpec conformance: constructor and getters.
 * @run main DSAParameterSpecTest
 */

public class DSAParameterSpecTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.math.BigInteger p = new java.math.BigInteger("11");
        java.math.BigInteger q = new java.math.BigInteger("5");
        java.math.BigInteger g = new java.math.BigInteger("2");
        java.security.spec.DSAParameterSpec s = new java.security.spec.DSAParameterSpec(p, q, g);
        check(p.equals(s.getP()) && q.equals(s.getQ()) && g.equals(s.getG()), "getP/getQ/getG");
        // ---- 契约深化：null 语义/引用保持/值语义/AlgorithmParameters 闭环 ----
        java.math.BigInteger p2 = java.math.BigInteger.valueOf(101);
        java.math.BigInteger q2 = java.math.BigInteger.valueOf(103);
        java.math.BigInteger g2 = java.math.BigInteger.valueOf(107);
        java.security.spec.DSAParameterSpec dp2 = new java.security.spec.DSAParameterSpec(p2, q2, g2);
        check(dp2.getP() == p2, "getP returns the same BigInteger reference");
        check(dp2.getQ() == q2, "getQ returns the same BigInteger reference");
        check(dp2.getG() == g2, "getG returns the same BigInteger reference");
        java.security.spec.DSAParameterSpec dpNull = new java.security.spec.DSAParameterSpec(null, null, null);
        check(dpNull.getP() == null, "null-args ctor: getP null");
        check(dpNull.getQ() == null, "null-args ctor: getQ null");
        check(dpNull.getG() == null, "null-args ctor: getG null");
        check(java.security.spec.DSAParameterSpec.class.isAssignableFrom(java.security.spec.DSAParameterSpec.class)
                && !java.security.spec.DSAParameterSpec.class.isAssignableFrom(java.math.BigInteger.class), "isAssignableFrom sanity");
        check(!dp2.equals(new java.security.spec.DSAParameterSpec(p2, q2, g2)), "no equals override: identity semantics");
        check(dp2.equals(dp2), "self-equality");
        check(dp2.hashCode() == dp2.hashCode(), "hashCode stable");
        check(dp2.toString().contains("DSAParameterSpec"), "default toString contains class name");
        check(!(dp2 instanceof java.io.Serializable), "not Serializable");
        check(new java.security.spec.DSAParameterSpec[0].getClass().getComponentType() == java.security.spec.DSAParameterSpec.class, "array component type");
        try {
            java.security.AlgorithmParameters ap = java.security.AlgorithmParameters.getInstance("DSA");
            ap.init(dp2);
            java.security.spec.DSAParameterSpec back = ap.getParameterSpec(java.security.spec.DSAParameterSpec.class);
            check(back != null, "AlgorithmParameters accepts DSAParameterSpec and returns one");
            check(p2.equals(back.getP()), "p survives engine init/getParameterSpec round-trip");
            check(q2.equals(back.getQ()), "q survives engine init/getParameterSpec round-trip");
            check(g2.equals(back.getG()), "g survives engine init/getParameterSpec round-trip");
            check(ap.getEncoded() != null && ap.getEncoded().length > 0, "ASN.1 encoding produced from spec");
        } catch (Exception e) {
            throw new AssertionError("AlgorithmParameters interop failed: " + e, e);
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
